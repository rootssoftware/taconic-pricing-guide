package be.roots.taconic.pricingguide.service;

/*
   This file is part of the Taconic Pricing Guide generator.  This code will
   generate a full featured PDF Pricing Guide by using using iText
   (http://www.itextpdf.com) based on JSON files.

   Copyright (C) 2015  Roots nv
   Authors: Koen Dehaen (koen.dehaen@roots.be)

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   For more information, please contact Roots nv at this address: support@roots.be
 */

import be.roots.taconic.pricingguide.domain.*;
import be.roots.taconic.pricingguide.pdfdomain.PDFCategory;
import be.roots.taconic.pricingguide.pdfdomain.PDFModel;
import be.roots.taconic.pricingguide.pdfdomain.PDFPricing;
import be.roots.taconic.pricingguide.pdfdomain.PDFPricingLine;
import be.roots.taconic.pricingguide.respository.TemplateRepository;
import be.roots.taconic.pricingguide.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static java.util.Arrays.asList;

@Service
public class PDFServiceImpl implements PDFService {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PDFServiceImpl.class);
    private final static String DELIMITER = "£ù£ù£ù£ù£ù£ù£ù£ù";

    private static final float COLUMN_RELATIVE_WIDTH_LEFT = 40f;
    private static final float COLUMN_RELATIVE_WIDTH_MIDDLE = 1f;
    private static final float COLUMN_RELATIVE_WIDTH_RIGHT = 59f;
    private static final float LEFT_MARGIN_COVER_TITLE = 325f;

    @Value("${url.base}")
    private String urlBase;

    @Value("${link.email}")
    private String emailLink;

    @Value("${link.website}")
    private String websiteLink;

    @Value("${link.contactUs}")
    private String contactUsUrl;

    @Value("${document.title}")
    private String documentTitle;

    @Value("${cover.title.1}")
    private String coverTitle1;

    @Value("${cover.title.2}")
    private String coverTitle2;

    @Value("${disclaimer}")
    private String disclaimer;

    private final DefaultService defaultService;

    private final TemplateRepository templateRepository;

    private final GitService gitService;

    private PDFTemplates pdfTemplate;

    public PDFServiceImpl(DefaultService defaultService, TemplateRepository templateRepository, GitService gitService) {
        this.defaultService = defaultService;
        this.templateRepository = templateRepository;
        this.gitService = gitService;
    }

    @PostConstruct
    private void init() throws IOException {

        final String urlAsString = defaultService.getBaseUrl() + "/pricing_guide_files/pdf_list.json";
        final String pdfTemplateAsJson = HttpUtil.readString(urlAsString, urlBase, defaultService.getUserName(), defaultService.getPassword());
        if (pdfTemplateAsJson == null) {
            LOGGER.error("Unable to open " + urlAsString);
            throw new RuntimeException("Unable to open " + urlAsString);
        }
        pdfTemplate = JsonUtil.asObject(pdfTemplateAsJson, PDFTemplates.class);

    }

    @Override
    public byte[] createPricingGuide(Contact contact, List<PDFModel> models) throws IOException, DocumentException {

        final Toc tableOfContents = new Toc();
        int numberOfModelAndTOCPages = 0;

        // add all template pages that should be placed before the Model pages
        byte[] guide = iTextUtil.merge(collectPages(pdfTemplate.getBefore(), tableOfContents, Toc.BEFORE_SORT_PREFIX));

        // add the "Models" paragraph to the Toc
        tableOfContents.addEntries(1, Collections.singletonList("Models"), null, true, Toc.MODEL_SORT_PREFIX);

        // add the Model pages to the pdf
        final byte[] modelPages = createModelPages(models, tableOfContents);
        numberOfModelAndTOCPages += iTextUtil.numberOfPages(modelPages);
        guide = iTextUtil.merge(guide, modelPages);

        // add all template pages that should be placed after the Model Pages
        guide = iTextUtil.merge(guide, collectPages(pdfTemplate.getAfter(), tableOfContents, Toc.AFTER_SORT_PREFIX));

        // add empty pages for the Table of Contents
        final byte[] addPagesForTableOfContents = addPagesForTableOfContents(tableOfContents);
        numberOfModelAndTOCPages += iTextUtil.numberOfPages(addPagesForTableOfContents) - 1;
        guide = iTextUtil.merge(guide, addPagesForTableOfContents);

        // reorder the complete PDF into the final sequence
        guide = iTextUtil.organize(guide, tableOfContents);

        // fix the background for pages without a template (= Table of Contents)
        guide = fixBackground(guide, tableOfContents, numberOfModelAndTOCPages);

        // enable the links on the Model pages
        guide = enableLinkToWebsite(guide, tableOfContents, numberOfModelAndTOCPages);

        // add page numbers to all pages
        guide = iTextUtil.setPageNumbers(guide);

        // create a Table of Contents in the bookmark section
        guide = createBookmarks(guide, tableOfContents);

        // create the Table of Contents on the Table of Contents pages
        guide = stampTableOfContents(guide, tableOfContents);

        // personalize the document
        guide = personalize(guide, contact, tableOfContents);

        // set the document properties
        guide = setDocumentProperties(contact, guide);

        return guide;
    }

    private byte[] setDocumentProperties(Contact contact, byte[] guide) throws IOException, DocumentException {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(guide);
            final PdfStamper stamper = new PdfStamper(reader, baos);

            final Map<String, String> info = reader.getInfo();

            info.put("Title", documentTitle);
            info.put("Subject", documentTitle);

            info.put(
                    "Keywords",
                    "Created for " + contact.getFullName() + ". " +
                            "Currency used : " + contact.getCurrency() + ". " +
                            "Mailed to " + contact.getEmail() + ". " +
                            "Build on " + gitService.getCommitId()
            );

            info.put("Creator", "Roots Software - https://www.roots.be - info@roots.be");
            info.put("Author", "Taconic Biosciences - https://www.taconic.com");

            stamper.setMoreInfo(info);

            stamper.close();
            reader.close();

            return baos.toByteArray();

        }
    }

    private byte[] createModelPages(List<PDFModel> models, Toc tableOfContents) throws IOException, DocumentException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = iTextUtil.createNewDocument();
        PdfWriter writer = PdfWriter.getInstance(document, bos);

        document.open();

        // create a PdfPTable for every model (so we can measure the height)
        final List<PdfPTable> modelPageTables = new ArrayList<>();
        Collections.sort(models);
        models
                .stream()
                .filter(Objects::nonNull)
                .map(this::createModelPage)
                .forEach(modelPageTables::add)
        ;

        // put the PdfPTable Models tables on PDF pages (multiple per page if possible)
        byte[] pages = new byte[]{};

        int height = 0;
        List<String> pageNames = new ArrayList<>();

        int i = 0;
        for (PdfPTable modelPageTable : modelPageTables) {

            // create a new pdf page, if necessary
            if (height != 0 && (height + modelPageTable.getTotalHeight() + 20 /* for the line separator */) >= iTextUtil.PAGE_HEIGHT) {

                writer.close();
                document.close();
                bos.close();

                byte[] page = bos.toByteArray();
                tableOfContents.addEntries(2, pageNames, page, true, Toc.MODEL_SORT_PREFIX + "___" + IntUtil.format(++i));
                pages = iTextUtil.merge(pages, page);

                height = 0;
                pageNames.clear();
                bos = new ByteArrayOutputStream();
                document = iTextUtil.createNewDocument();
                writer = PdfWriter.getInstance(document, bos);

                document.open();

            } else if (height != 0) {
                // if not the first model on the page, draw a separator

                document.add(new Paragraph(new Chunk(new LineSeparator(.25f, 80f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2))));
                document.add(new Paragraph(new Chunk("   ")));
            }

            // rerender the table (with a valid pdfWriter)
            document.add(createModelPage(models.get(modelPageTables.indexOf(modelPageTable))));
            height += modelPageTable.getTotalHeight();
            pageNames.add(models.get(modelPageTables.indexOf(modelPageTable)).getProductNameProcessed());

        }
        writer.close();
        document.close();

        final byte[] page = bos.toByteArray();
        tableOfContents.addEntries(2, pageNames, page, true, Toc.MODEL_SORT_PREFIX + "___" + IntUtil.format(++i));
        pages = iTextUtil.merge(pages, page);

        return pages;

    }

    private byte[] addPagesForTableOfContents(Toc tableOfContents) throws IOException, DocumentException {
        final int numberOfPages = tableOfContents.getNumberOfPages();
        tableOfContents.addTocEntry(numberOfPages);
        byte[] pages = new byte[]{};
        for (int i = 0; i < numberOfPages; i++) {
            pages = iTextUtil.merge(pages, iTextUtil.emptyPage());
        }
        return pages;
    }

    private byte[] fixBackground(byte[] pdf, Toc tableOfContents, int numberOfModelAndTOCPages) throws IOException, DocumentException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final byte[] tocTemplate = templateRepository.findOne(pdfTemplate.getTocTemplate().getUrl());
            final byte[] modelPageTemplate = templateRepository.findOne(pdfTemplate.getModel().getUrl());

            final Image tocBackgroundImage = iTextUtil.getImageFromPdf(tocTemplate);
            tocBackgroundImage.setAbsolutePosition(0, 0);
            tocBackgroundImage.setInverted(false);

            final Image modelPageBackgroundImage = iTextUtil.getImageFromPdf(modelPageTemplate);
            modelPageBackgroundImage.setAbsolutePosition(0, 0);
            modelPageBackgroundImage.setInverted(false);

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            final int firstPageOfToc = tableOfContents.getFirstPageOfToc();
            final PdfContentByte tocContent = stamper.getUnderContent(firstPageOfToc);

            final float pageWidth = reader.getPageSize(1).getWidth();
            final float pageHeight = tocBackgroundImage.getHeight() / (tocBackgroundImage.getHeight() / reader.getPageSize(1).getHeight());

            tocContent.addImage(tocBackgroundImage, pageWidth, 0, 0, pageHeight, 0, 0);

            for (int pageNumber = firstPageOfToc + 1; pageNumber <= firstPageOfToc + numberOfModelAndTOCPages; pageNumber++) {

                final PdfContentByte content = stamper.getUnderContent(pageNumber);
                content.addImage(modelPageBackgroundImage, pageWidth, 0, 0, pageHeight, 0, 0);

            }

            stamper.close();
            reader.close();

            return bos.toByteArray();
        }

    }

    // combines a list of pages together
    private byte[] collectPages(List<Template> pages, Toc tableOfContents, String sortPrefix) throws IOException, DocumentException {
        byte[] pdf = new byte[]{};
        int i = 1;
        for (Template pdfTemplate : pages) {
            if (!pdfTemplate.isTocTemplate()) {
                final byte[] template = templateRepository.findOne(pdfTemplate.getUrl());
                pdf = iTextUtil.merge(pdf, template);
                tableOfContents.addEntries(1, Collections.singletonList(pdfTemplate.getName()), template, pdfTemplate.isToc(), sortPrefix + "___" + IntUtil.format(i++));
            }
        }
        return pdf;
    }

    private PdfPTable createModelPage(PDFModel model) {

        final PdfPTable pdfPTable = new PdfPTable(new float[]{COLUMN_RELATIVE_WIDTH_LEFT, COLUMN_RELATIVE_WIDTH_MIDDLE, COLUMN_RELATIVE_WIDTH_RIGHT});
        pdfPTable.setTotalWidth(iTextUtil.PAGE_SIZE.getWidth());
        final PdfPTable modelPricingTables = buildModelPricingTables(model);
        modelPricingTables.setTotalWidth(iTextUtil.PAGE_SIZE.getWidth() / 100 * COLUMN_RELATIVE_WIDTH_RIGHT);

        final int numberOfPages = countNumberOfPagesForModelPricingTables(modelPricingTables);

        if (numberOfPages > 1) {
            final PdfPTable modelDetailSectionTable = new PdfPTable(1);
            modelDetailSectionTable.setTotalWidth(COLUMN_RELATIVE_WIDTH_LEFT);
            for (int i = 0; i < numberOfPages; i++) {
                final PdfPCell cell = cell(buildModelDetailSection(model));
                cell.setFixedHeight(iTextUtil.PAGE_HEIGHT + 1);
                modelDetailSectionTable.addCell(cell);
            }
            pdfPTable.addCell(cell(modelDetailSectionTable));
        } else {
            pdfPTable.addCell(cell(buildModelDetailSection(model)));
        }
        pdfPTable.addCell(cell(new Paragraph()));
        pdfPTable.addCell(cell(modelPricingTables));
        return pdfPTable;

    }

    // iText is not able to render the precise height of the table, so we render the subsection of the document to check how many pages the tables require
    private int countNumberOfPagesForModelPricingTables(PdfPTable modelPricingTables) {

        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final Document document = iTextUtil.createNewDocument();
            final PdfWriter writer = PdfWriter.getInstance(document, bos);
            document.open();

            document.add(modelPricingTables);

            writer.close();
            document.close();

            return iTextUtil.numberOfPages(bos.toByteArray());
        } catch (DocumentException | IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    private Phrase processHtmlCodesAsPhrase(String name, Font baseFont, Font symbol) {
        final Phrase phrase = new Phrase();
        processHtmlCodes(new Paragraph(), name, baseFont, symbol)
                .stream()
                .map(Phrase::getChunks)
                .flatMap(List::stream)
                .forEach(phrase::add);
        return phrase;
    }

    private List<Paragraph> processHtmlCodes(Paragraph firstParagraph, String name, Font baseFont, Font symbol) {

        final Font italicFont = new Font(baseFont);
        italicFont.setStyle(Font.FontStyle.ITALIC.getValue());

        final Font normalFont = new Font(baseFont);

        final List<Paragraph> paragraphs = new ArrayList<>();
        paragraphs.add(firstParagraph);

        if (!StringUtils.isEmpty(name)) {

            for (String[] alphabet : GreekAlphabet.getAlphabet()) {
                name = name.replaceAll(alphabet[0], DELIMITER + alphabet[0] + DELIMITER);
            }

            name = SpecialCharactersUtil.encode(
                    name.replaceAll("<[s|S][u|U][p|P]>", DELIMITER + "<sup>")
                            // introduce special formatting ea
                            .replaceAll("</[s|S][u|U][p|P]>", DELIMITER)
                            .replaceAll("<[i|I]>|<[e|E][m|M]>", DELIMITER + "<i>")
                            .replaceAll("</[i|I]>|</[e|E][m|M]>", DELIMITER + "</i>")
                            .replaceAll("<[l|L][i|I]>", DELIMITER + "<li>")
                            .replaceAll("<[h|H][r|R]>", DELIMITER + "<hr>")
                            .replaceAll("(<[a|A] .*?<\\/[a|A]>)", DELIMITER + "$1" + DELIMITER)
                            .replaceAll("<[p|P].*?>(.*?)<\\/[p|P]>", "$1" + DELIMITER + "<br/>")
                            // introduce new lines
                            .replaceAll("<[d|D][i|I][v|V]>", DELIMITER + "<br/>")
                            .replaceAll("<[u|U][l|L].*?>", DELIMITER + "<br/>")
                            .replaceAll("</[l|L][i|I]>", DELIMITER + "<br/>")
                            .replaceAll("(<[b|B][r|R]\\s*\\/?>)", DELIMITER + "<br/>")
                            // remove some
                            .replaceAll("<\\/[u|U][l|L]>", "")
                            .replaceAll("</[a|A]>", "")
                            .replaceAll("<\\/?[b|B]>", "")
                            .replaceAll("<\\/?[h|H][0-9]>", "")
                            .replaceAll("<\\/[d|D][i|I][v|V]>", "")
                            .replaceAll("<\\/?[s|S][t|T][r|R][o|O][n|N][g|G]>", "")
                            // remove duplicated
                            .replaceAll("\\s\\s+", " ")  // replace multiple spaces with one
                            .replaceAll("(" + DELIMITER + ")+", DELIMITER)  // replace multiple spaces with one
                            .replaceAll("(" + DELIMITER + "<br/>)+", DELIMITER + "<br/>")  // replace multiple spaces with one
            );

            Arrays.stream(name.split(DELIMITER))
                    .filter(StringUtils::hasText)
                    .forEach(token -> {

                        final Paragraph currentParagraph = paragraphs.get(paragraphs.size() - 1);
                        Font usedFont = normalFont;
                        String text = token;
                        if (text.startsWith("<i>")) {
                            usedFont = italicFont;
                            text = text.substring(3);
                        } else if (text.startsWith("</i>")) {
                            text = text.substring(4);
                        }

                        usedFont.setSize(baseFont.getSize());

                        if (text.startsWith("&")) {

                            final char replacement = GreekAlphabet.getReplacement(text);
                            if (!Character.isWhitespace(replacement)) {
                                currentParagraph.add(SpecialSymbol.get(replacement, symbol));
                            } else {
                                currentParagraph.add(new Chunk(text, usedFont));
                            }

                        } else if (text.startsWith("<sup>")) {

                            final Font superScriptFont = new Font(usedFont);
                            superScriptFont.setSize(baseFont.getSize() * .6f);

                            final Chunk superScript = new Chunk(text.substring(5), superScriptFont);
                            superScript.setTextRise(3f);
                            currentParagraph.add(superScript);

                        } else if (text.startsWith("<hr>")) {

                            final LineSeparator line = new LineSeparator();
                            line.setLineColor(iTextUtil.getHeaderColor());
                            line.setLineWidth(0.5f);

                            currentParagraph.add(new Chunk("\n"));
                            currentParagraph.add(new Chunk(line));
                            currentParagraph.add(new Chunk("\n"));
                            currentParagraph.add(new Chunk(StringUtils.trimLeadingWhitespace(text.substring(4)), usedFont));

                        } else if (text.startsWith("<br/>")) {
                            currentParagraph.add(new Chunk("\n" + StringUtils.trimLeadingWhitespace(text.substring(5)), usedFont));
                        } else if (text.startsWith("<li>")) {
                            final Paragraph listItem = new Paragraph();
                            listItem.setTabSettings(new TabSettings(asList(new TabStop(10f, TabStop.Alignment.LEFT))));
                            listItem.setFirstLineIndent(-10f);
                            listItem.setIndentationLeft(10f);
                            listItem.add(new Chunk("\u2022", usedFont));
                            listItem.add(Chunk.TABBING);
                            listItem.add(new Chunk(StringUtils.trimLeadingWhitespace(text.substring(4)), usedFont));
                            paragraphs.add(listItem);
                        } else if (text.matches("<[a|A].*[h|H][r|R][e|E][f|F]*[^>]*>.*")) {

                            final Font linkFont = new Font(usedFont);
                            linkFont.setColor(iTextUtil.getLinkColor());

                            final Chunk chunk = new Chunk(text.replaceAll("<[a|A].*?[>]", ""), linkFont);
                            chunk.setAction(new PdfAction(text.replaceAll(".*[h|H][r|R][e|E][f|F]=['|\"](.*?)['|\"].*", "$1")));
                            currentParagraph.add(chunk);

                        } else {
                            currentParagraph.add(new Chunk(text, usedFont));
                        }
                    });

        }

        return paragraphs;
    }

    private PdfPTable buildModelPricingTables(PDFModel model) {

        final PdfPTable table = new PdfPTable(1);
        table.setSplitLate(false); // this sees to it that if the first table spans over different pages, the first chunck of the pricingTable is shown on the first page

        final List<PDFCategory> categories = model.getCategories();

        if (CollectionUtils.isEmpty(categories)) {
            final Chunk chunk = new Chunk("Contact us for pricing on this model", iTextUtil.getFontContactUs());
            chunk.setAction(new PdfAction(contactUsUrl));
            table.addCell(cell(new Phrase(chunk)));
        } else {
            categories.forEach(category -> buildModelPricingTables(table, category));
        }

        return table;

    }

    private void buildModelPricingTables(PdfPTable table, PDFCategory category) {

        // build the pricing table title
        final PdfPTable titleTable = new PdfPTable(1);
        titleTable.addCell(cell(processHtmlCodesAsPhrase(category.getNameAsTitle(), iTextUtil.getFontModelCategory(), iTextUtil.getFontModelSymbol())));
        titleTable.addCell(cell(new Paragraph(" ")));

        // combine both tables into one table
        final PdfPTable modelTable = new PdfPTable(1);
        modelTable.setTotalWidth(iTextUtil.PAGE_SIZE.getWidth() / 100 * COLUMN_RELATIVE_WIDTH_RIGHT);
        modelTable.addCell(cell(titleTable));
        if (category.getStandard().hasLines()) {
            modelTable.addCell(cell(buildModelPricingTables(category.getStandard(), false)));
        }
        if (category.getSpecialized().hasLines()) {
            final PdfPTable subTitleTable = new PdfPTable(1);
            final PdfPCell subTitleCell = cell(new Paragraph("Specialized Inventory", iTextUtil.getFontModelSpecializedInventory()));
            subTitleCell.setFixedHeight(15f);
            subTitleTable.addCell(subTitleCell);

            modelTable.addCell(cell(subTitleTable));
            modelTable.addCell(cell(buildModelPricingTables(category.getSpecialized(), true)));
        }

        if (!StringUtils.isEmpty(category.getMessage())) {
            modelTable.addCell(cell(new Paragraph(category.getMessage(), iTextUtil.getFontModelPricingMessage())));
            modelTable.addCell(cell(new Paragraph(" ")));
        }

        if (modelTable.getTotalHeight() < iTextUtil.PAGE_HEIGHT) {
            // if the pricing table can fit on a page, then keep it together and if necessary break to the next page
            modelTable.keepRowsTogether(0);
        } else {
            // if the pricing table only fits on multiple pages, then ask to split as fast as it can, and keep the first part on the first table
            modelTable.setSplitLate(false);
        }

        table.addCell(cell(modelTable));

    }

    private PdfPTable buildModelPricingTables(PDFPricing pricing, boolean specialized) {

        // build the pricing table details
        final PdfPTable detailsTable = new PdfPTable(pricing.getNumberOfHeaderItems());
        detailsTable.setHeaderRows(1); // to re-print the header on each page if the table splits over multiple pages
        if (pricing.hasAge()) {
            detailsTable.addCell(cellH(new Paragraph(specialized ? "Age / Type" : "Age in weeks", iTextUtil.getFontModelPricingTitle())));
        }
        if (pricing.hasQuantities()) {
            final PdfPCell cell = cellH(new Paragraph("Quantity", iTextUtil.getFontModelPricingTitle()));
            cell.setColspan(pricing.getQuantities().size());
            detailsTable.addCell(cell);
        }
        if (pricing.hasAge()) {
            detailsTable.addCell(cellH(new Paragraph("", iTextUtil.getFontModelPricingTitle())));
        }
        if (pricing.hasQuantities()) {
            pricing.getQuantities()
                    .forEach(q -> detailsTable.addCell(cellH(new Paragraph(q, iTextUtil.getFontModelPricingTitle()))));
        }
        boolean invert = false;

        for (final PDFPricingLine pricingLine : pricing.getPricingLines()) {
            if (pricing.hasAge()) {
                detailsTable.addCell(cellD(new Paragraph(pricingLine.getAge(), iTextUtil.getFontModelPricingData()), invert));
            }
            if (pricing.hasQuantities()) {
                final boolean invertColor = invert;
                pricing.getQuantities()
                        .forEach(q -> detailsTable.addCell(cellD(new Paragraph(pricingLine.getPrice(q), iTextUtil.getFontModelPricingData()), invertColor)));
            }
            invert = !invert;
        }

        detailsTable.addCell(cell(new Paragraph(" "), pricing.getNumberOfHeaderItems()));

        return detailsTable;

    }

    private PdfPTable buildModelDetailSection(PDFModel model) {

        final Phrase p = processHtmlCodesAsPhrase(model.getProductNameProcessed(), iTextUtil.getFontModelTitle(), iTextUtil.getFontModelSymbol());
        for (Chunk c : p.getChunks()) {
            c.setAction(new PdfAction(model.getUrl()));
        }

        final StringBuilder strAppList = new StringBuilder();
        for (String application : model.getApplicationsSorted()) {
            if (strAppList.length() != 0) {
                strAppList.append(", ");
            }
            strAppList.append(application);
        }

        final PdfPTable table = new PdfPTable(1);
        table.setSplitLate(false);

        table.addCell(cell(p));

        table.addCell(createRow("Model Number", model.getModelNumber(), null));
        table.addCell(createRow("Animal Type", model.getAnimalType(), null));
        table.addCell(createRow("Nomenclature", model.getNomenclatureParsed(), null));
        table.addCell(createRow("Application(s)", strAppList.toString(), null));
        table.addCell(createRow("Health Report", model.getHealthReport(), model.getHealthReport()));
        table.addCell(createRow("Species", model.getSpecies(), null));
        table.addCell(createRow("Licensing", model.getLicense(), null));

        table.addCell(cell(createOrderButton(model)));

        return table;

    }

    private PdfPTable createOrderButton(PDFModel model) {

        final Chunk chunk = new Chunk("Order on taconic.com", iTextUtil.getFontButton());
        chunk.setAction(new PdfAction("http://www.taconic.com/start-an-order?modelNumber=" + model.getModelNumber()));

        final PdfPCell cell = cell(new Phrase(chunk));
        cell.setBackgroundColor(iTextUtil.getTaconicButton());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(8);

        final PdfPTable button = new PdfPTable(new float[]{80f, 20f});
        button.addCell(cell(new Phrase(" "), 2));
        button.addCell(cell);
        button.addCell(cell(new Phrase(" ")));
        button.addCell(cell(new Phrase(" "), 2));

        return button;
    }

    private PdfPCell createRow(String key, String value, String url) {
        final Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(key, iTextUtil.getFontModelKey()));
        paragraph.add(new Chunk(": ", iTextUtil.getFontModelKey()));

        if (value == null) {
            value = "";
        }

        final PdfPCell cell = cell(new Phrase(""));
//        cell.setPaddingBottom(5f);

        processHtmlCodes(paragraph, value.trim(), iTextUtil.getFontModelValue(), iTextUtil.getFontModelSymbol())
                .stream()
                .peek(p ->
                        p.getChunks()
                                .forEach(chunk -> {
                                    chunk.setLineHeight(13f);
                                    if (StringUtils.hasText(url))
                                        chunk.setAction(new PdfAction(url));
                                })
                )
                .forEach(cell::addElement);

        return cell;
    }

    private PdfPCell cell(Phrase p, int colSpan) {
        final PdfPCell cell = cell(p);
        cell.setColspan(colSpan);
        return cell;
    }

    private PdfPCell cell(Phrase p) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell cellD(Phrase p, boolean invert) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (invert) {
            cell.setBackgroundColor(iTextUtil.getRowInvertColor());
        }
        return cell;
    }

    private PdfPCell cellH(Phrase p) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(iTextUtil.getHeaderColor());
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell cell(PdfPTable p) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        return cell;
    }

    private byte[] personalize(byte[] pdf, Contact contact, Toc tableOfContents) throws
            IOException, DocumentException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            // stamp some text on first page
            PdfContentByte text = stamper.getOverContent(1);

            text.beginText();

            text.setColorFill(iTextUtil.getFontCoverPricingguide().getColor());
            text.setFontAndSize(iTextUtil.getFontCoverPricingguide().getBaseFont(), iTextUtil.getFontCoverPricingguide().getSize());
            text.setCharacterSpacing(-1f);
            text.showTextAligned(Element.ALIGN_LEFT, coverTitle1, LEFT_MARGIN_COVER_TITLE, 195, 0);

            text.setColorFill(iTextUtil.getFontCoverYear().getColor());
            text.setFontAndSize(iTextUtil.getFontCoverYear().getBaseFont(), iTextUtil.getFontCoverYear().getSize());
            text.setCharacterSpacing(-12f);
            text.showTextAligned(Element.ALIGN_LEFT, coverTitle2, LEFT_MARGIN_COVER_TITLE, 105, 0);

            text.setColorFill(iTextUtil.getFontCoverTriangle().getColor());
            text.setFontAndSize(iTextUtil.getFontCoverTriangle().getBaseFont(), iTextUtil.getFontCoverTriangle().getSize());
            text.setCharacterSpacing(0f);
            text.showTextAligned(Element.ALIGN_LEFT, "u", LEFT_MARGIN_COVER_TITLE, 80, 0);

            text.setColorFill(iTextUtil.getFontCoverCurrency().getColor());
            text.setFontAndSize(iTextUtil.getFontCoverCurrency().getBaseFont(), iTextUtil.getFontCoverCurrency().getSize());
            text.setCharacterSpacing(0f);
            text.showTextAligned(Element.ALIGN_LEFT, contact.getCurrency().getTitlePageDescription(), LEFT_MARGIN_COVER_TITLE + 15, 80, 0);

            text.endText();

            // stamp some text on first page of the table of contents page
            final Image logoImage = iTextUtil.getImageFromByteArray(HttpUtil.readByteArray(pdfTemplate.getLogo().getUrl(), urlBase, defaultService.getUserName(), defaultService.getPassword()));
            final PdfContentByte tocContent = stamper.getOverContent(tableOfContents.getFirstPageOfToc());
            final float resizeRatio = logoImage.getHeight() / 85; // define the desired height of the log
            tocContent.addImage(logoImage, logoImage.getWidth() / resizeRatio, 0, 0, logoImage.getHeight() / resizeRatio, 59, 615);

            text = stamper.getOverContent(tableOfContents.getFirstPageOfToc());

            text.beginText();

            text.setColorFill(iTextUtil.getFontPersonalization().getColor());
            text.setFontAndSize(iTextUtil.getFontPersonalization().getBaseFont(), iTextUtil.getFontPersonalization().getSize());
            text.showTextAligned(Element.ALIGN_LEFT, "Prepared for:", 355, 681, 0);
            text.showTextAligned(Element.ALIGN_LEFT, contact.getFullName(), 355, 662, 0);

            // set company name
            if (!StringUtils.isEmpty(contact.getCompany())) {
                text.showTextAligned(Element.ALIGN_LEFT, contact.getCompany(), 355, 643, 0);
                text.showTextAligned(Element.ALIGN_LEFT, new SimpleDateFormat("MM-dd-yyyy").format(new Date()), 355, 624, 0);
            } else {
                text.showTextAligned(Element.ALIGN_LEFT, new SimpleDateFormat("MM-dd-yyyy").format(new Date()), 355, 643, 0);
            }

            text.endText();

            final ColumnText ct = new ColumnText(tocContent);
            ct.setSimpleColumn(new Rectangle(55, 517, iTextUtil.PAGE_SIZE.getWidth() - 45, 575));
            final List<Element> elements = HTMLWorker.parseToList(new StringReader(disclaimer), null);
            final Paragraph p = new Paragraph();
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            for (Element element : elements) {
                for (Chunk chunk : element.getChunks()) {
                    chunk.setFont(iTextUtil.getFontDisclaimer());
                }
                p.add(element);
            }
            ct.addElement(p);
            ct.go();

            stamper.close();
            reader.close();
            return bos.toByteArray();

        }

    }

    private byte[] createBookmarks(byte[] pdf, Toc tableOfContents) throws DocumentException, IOException {

        // create the bookmarks
        final List<HashMap<String, Object>> outlines = new ArrayList<>();
        final List<TocEntry> entriesSorted = tableOfContents.getEntriesSorted();
        final List<HashMap<String, Object>> modelBookmarkKids = new ArrayList<>();
        HashMap<String, Object> modelBookmark;

        for (TocEntry tocEntry : entriesSorted) {

            if (tocEntry.isIncludedInToc()) {

                final HashMap<String, Object> bookmark = new HashMap<>();

                String name = tocEntry.getName()
                        .replaceAll("<sup>", "")
                        .replaceAll("</sup>", "")
                        .replaceAll("<i.*?>", "")
                        .replaceAll("</i>", "");
                name = GreekAlphabet.replaceGreekHtmlCodesWithUnicode(name);

                bookmark.put("Title", name);
                bookmark.put("Action", "GoTo");
                bookmark.put("Page", String.format("%d Fit", tocEntry.getFinalPageNumber()));
                if (tocEntry.getLevel() == 1) {
                    outlines.add(bookmark);
                } else {
                    modelBookmarkKids.add(bookmark);
                }

                if (tocEntry.isModelHeader()) {
                    modelBookmark = bookmark;
                    modelBookmark.put("Open", true);
                    modelBookmark.put("Kids", modelBookmarkKids);
                }

            }

        }

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            stamper.setOutlines(outlines);
            stamper.close();
            reader.close();

            return bos.toByteArray();
        }

    }

    private byte[] stampTableOfContents(byte[] pdf, Toc tableOfContents) throws IOException, DocumentException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            // stamp the named destinations
            for (int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++) {
                stamper.addNamedDestination("page" + pageNumber, pageNumber, new PdfDestination(PdfDestination.XYZ, 80f, 800f, 0));
            }

            // create the table of contents
            final Chunk tocTitle = new Chunk("TABLE OF CONTENTS", iTextUtil.getFontTocTitle());

            int currentTocPage = tableOfContents.getFirstPageOfToc();
            int firstTocPage = currentTocPage;
            PdfContentByte canvas = stamper.getOverContent(currentTocPage);

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(tocTitle), 55, 470, 0);

            final List<TocEntry> entriesSorted = tableOfContents.getEntriesSorted();
            int tocEntryNumber = 0;
            for (TocEntry tocEntry : entriesSorted) {

                if (tocEntry.isIncludedInToc()) {
                    tocEntryNumber++;

                    // take the right TOC page to stamp the TOC entry on (needed for TOC's with multiple pages)
                    if (tocEntryNumber == getNumberOfItemsPerTocPage(0) + 1 ||
                            (tocEntryNumber > getNumberOfItemsPerTocPage(0) &&
                                    (tocEntryNumber - getNumberOfItemsPerTocPage(0)) % (getNumberOfItemsPerTocPage(currentTocPage - firstTocPage) + 1) == 0)) {
                        currentTocPage++;
                        canvas = stamper.getOverContent(currentTocPage);
                    }

                    Font font = iTextUtil.getFontToc();
                    if (tocEntry.getLevel() == 1) {
                        font = iTextUtil.getFontTocBold();
                    }

                    final Phrase p = processHtmlCodesAsPhrase(tocEntry.getLevelString() + tocEntry.getName(), font, iTextUtil.getFontTocSymbol());
                    p.add(new Chunk("", iTextUtil.getFontToc()));
                    if (tocEntry.isShowingPageNumber()) {
                        p.add(new Chunk(new DottedLineSeparator()));
                        p.add(new Chunk("  " + tocEntry.getFinalPageNumber(), iTextUtil.getFontToc()));
                    }

                    for (Chunk chunk : p.getChunks()) {
                        chunk.setAction(PdfAction.gotoLocalPage("page" + tocEntry.getFinalPageNumber(), false));
                    }

                    int y;
                    if (tocEntryNumber <= getNumberOfItemsPerTocPage(0)) {
                        y = 460 - (16 * tocEntryNumber);
                    } else {
                        y = 680 - (16 * ((tocEntryNumber - getNumberOfItemsPerTocPage(0)) % (getNumberOfItemsPerTocPage(currentTocPage - firstTocPage) + 1)));
                    }

                    final ColumnText ct = new ColumnText(canvas);
                    ct.setSimpleColumn(p, 52, y, 555, 70, 0, Element.ALIGN_JUSTIFIED);
                    ct.go();

                }
            }

            stamper.close();
            reader.close();

            return bos.toByteArray();

        }

    }

    private byte[] enableLinkToWebsite(byte[] pdf, Toc tableOfContents, int numberOfModelAndTOCPages) throws
            IOException, DocumentException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            final int firstPageOfToc = tableOfContents.getFirstPageOfToc();
            for (int i = firstPageOfToc; i <= firstPageOfToc + numberOfModelAndTOCPages; i++) {

                final Chunk websiteChunk = new Chunk("[.....................]");
                websiteChunk.setAction(new PdfAction(websiteLink));

                ColumnText ct = new ColumnText(stamper.getUnderContent(i));
                ct.setSimpleColumn(180, 25, 335, 64);
                ct.addText(new Phrase(websiteChunk));
                ct.go();

                final Chunk emailChunk = new Chunk("[..............................]");
                emailChunk.setAction(new PdfAction(emailLink));

                ct = new ColumnText(stamper.getUnderContent(i));
                ct.setSimpleColumn(335, 12, 460, 53);
                ct.addText(new Phrase(emailChunk));
                ct.go();

            }

            stamper.close();
            reader.close();
            return bos.toByteArray();

        }

    }

    public static int getNumberOfItemsPerTocPage(int pageNumber) {
        return pageNumber == 0 ? 24 : 38;
    }

}