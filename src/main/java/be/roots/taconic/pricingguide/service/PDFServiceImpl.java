package be.roots.taconic.pricingguide.service;

/**
 *  This file is part of the Taconic Pricing Guide generator.  This code will
 *  generate a full featured PDF Pricing Guide by using using iText
 *  (http://www.itextpdf.com) based on JSON files.
 *
 *  Copyright (C) 2015  Roots nv
 *  Authors: Koen Dehaen (koen.dehaen@roots.be)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  For more information, please contact Roots nv at this address: support@roots.be
 *
 */

import be.roots.taconic.pricingguide.domain.*;
import be.roots.taconic.pricingguide.respository.TemplateRepository;
import be.roots.taconic.pricingguide.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service
public class PDFServiceImpl implements PDFService {

    private final static Logger LOGGER = Logger.getLogger(PDFServiceImpl.class);
    private final static String DELIMETER = "-!-!-!-!-!-!-!-";

    public static final int NUMBER_OF_ITEMS_PER_TOC_PAGE = 39;

    @Value("${link.email}")
    private String emailLink;

    @Value("${link.website}")
    private String websiteLink;

    @Value("${link.contactUs}")
    private String contactUsUrl;

    @Value("${document.title}")
    private String documentTitle;

    @Autowired
    private DefaultService defaultService;

    @Autowired
    private TemplateRepository templateRepository;

    private PDFTemplates pdfTemplate;

    @PostConstruct
    private void init() throws IOException {

        final String urlAsString = defaultService.getBaseUrl() + "/pricing_guide_files/pdf_list.json";
        final String pdfTemplateAsJson = HttpUtil.readString(urlAsString, defaultService.getUserName(), defaultService.getPassword());
        if ( pdfTemplateAsJson == null ) {
            LOGGER.error ( "Unable to open " + urlAsString );
            throw new RuntimeException("Unable to open " + urlAsString);
        }
        pdfTemplate = JsonUtil.asObject(pdfTemplateAsJson, PDFTemplates.class);

    }

    @Override
    public byte[] createPricingGuide(Contact contact, List<Model> models) throws IOException, DocumentException {

        final Toc tableOfContents = new Toc();
        byte[] guide = new byte[]{};

        // add all template pages that should be placed before the Model pages
        guide = iTextUtil.merge(collectPages(pdfTemplate.getBefore(), tableOfContents, contact, Toc.BEFORE_SORT_PREFIX));

        // add the "Models" paragraph to the Toc
        tableOfContents.addEntries(1, Arrays.asList("Models"), null, true, Toc.MODEL_SORT_PREFIX);

        // add the Model pages to the pdf
        guide = iTextUtil.merge(guide, createModelPages(contact, models, tableOfContents));

        // add all template pages that should be placed after the Model Pages
        guide = iTextUtil.merge(guide, collectPages(pdfTemplate.getAfter(), tableOfContents, contact, Toc.AFTER_SORT_PREFIX));

        // add empty pages for the Table of Contents
        guide = iTextUtil.merge(guide, addPagesForTableOfContents(tableOfContents));

        // reorder the complete PDF into the final sequence
        guide = iTextUtil.organize(guide, tableOfContents);

        // fix the background for pages without a template (= Table of Contents)
        guide = fixBackground(guide, tableOfContents);

        // enable the links on the Model pages
        guide = enableLinkToWebsite(guide, tableOfContents);

        // add page numbers to all pages
        guide = iTextUtil.setPageNumbers(guide);

        // create a Table of Contents in the bookmark section
        guide = createBookmarks(guide, tableOfContents);

        // create the Table of Contents on the Table of Contents pages
        guide = stampTableOfContents(guide, tableOfContents);

        // set the document properties
        guide = setDocumentProperties(contact, guide);

        return guide;
    }

    private byte[] setDocumentProperties(Contact contact, byte[] guide) throws IOException, DocumentException {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(guide);
            final PdfStamper stamper = new PdfStamper(reader, baos );

            final Map<String, String> info = reader.getInfo();

            info.put ("Title", documentTitle );
            info.put ("Subject", documentTitle );

            info.put("Keywords",
                        "Created for " + contact.getFullName() + ". " +
                        "Currency used : " + contact.getCurrency() + ". " +
                        "Mailed to " + contact.getEmail() + ". "
            );

            info.put("Creator", "Roots Software - http://www.roots.be - info@roots.be");
            info.put("Author", "Taconic - http://www.taconic.com");

            stamper.setMoreInfo(info);

            stamper.close();
            reader.close();

            return baos.toByteArray();

        }
    }

    private byte[] createModelPages(Contact contact, List<Model> models, Toc tableOfContents) throws IOException, DocumentException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = iTextUtil.createNewDocument();
        PdfWriter writer = PdfWriter.getInstance(document, bos);

        document.open();

        // create a PdfPTable for every model (so we can measure the height)
        final List<PdfPTable> modelPageTables = new ArrayList<>();
        Collections.sort(models);
        for (Model model : models) {
            if ( model != null ) {
                modelPageTables.add(createModelPage(contact, model, writer));
            }
        }

        // put the PdfPTable Models tables on PDF pages (multiple per page if possible)
        byte[] pages = new byte[]{};

        int height = 0;
        List<String> pageNames = new ArrayList<>();

        int i = 0;
        for ( PdfPTable modelPageTable : modelPageTables ) {

            // create a new pdf page, if necessary
            if ( height != 0 && ( height + modelPageTable.getTotalHeight() + 20 /* for the line separator */ ) >= iTextUtil.PAGE_HEIGHT ) {

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

            } else if ( height != 0 ) {
                // if not the first model on the page, draw a separator

                document.add (new Paragraph(new Chunk(new LineSeparator(.25f, 80f, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2 )) ));
                document.add (new Paragraph(new Chunk("   ") ));
            }

            // rerender the table (with a valid pdfWriter)
            document.add(createModelPage(contact, models.get(modelPageTables.indexOf(modelPageTable)), writer));
            height += modelPageTable.getTotalHeight();
            pageNames.add ( models.get ( modelPageTables.indexOf(modelPageTable) ).getProductNameProcessed() );

        }
        writer.close();
        document.close();

        byte[] page = bos.toByteArray();
        tableOfContents.addEntries(2, pageNames, page, true, Toc.MODEL_SORT_PREFIX + "___" + IntUtil.format(++i));
        pages = iTextUtil.merge(pages, page);

        return pages;

    }

    private byte[] addPagesForTableOfContents(Toc tableOfContents) throws IOException, DocumentException {
        tableOfContents.addTocEntry(tableOfContents.getNumberOfPages());
        byte[] pages = new byte[]{};
        for (int i = 0; i < tableOfContents.getNumberOfPages(); i ++ ) {
            pages = iTextUtil.merge(pages, iTextUtil.emptyPage());
        }
        return pages;
    }

    private byte[] fixBackground(byte[] pdf, Toc tableOfContents) throws IOException, DocumentException {

        try ( final ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {

            final byte[] tocTemplate = templateRepository.findOne (pdfTemplate.getTocTemplate().getUrl() );
            final byte[] modelPageTemplate = templateRepository.findOne (pdfTemplate.getModel().getUrl() );

            final Image tocBackgroundImage = iTextUtil.getImageFromPdf(tocTemplate);
            tocBackgroundImage.setAbsolutePosition(0, 0);

            final Image modelPageBackgroundImage = iTextUtil.getImageFromPdf(modelPageTemplate);
            modelPageBackgroundImage.setAbsolutePosition(0, 0);

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            final PdfContentByte tocContent = stamper.getUnderContent(tableOfContents.getFirstPageOfToc());
            tocContent.addImage(tocBackgroundImage, 612, 0, 0, 792, 0, 0);

            for ( int pageNumber = tableOfContents.getFirstPageOfToc() + 1; pageNumber <= tableOfContents.getLastPageNumberOfModelPages(); pageNumber ++ ) {

                final PdfContentByte content = stamper.getUnderContent(pageNumber);
                content.addImage(modelPageBackgroundImage, 612, 0, 0, 792, 0, 0);

            }

            stamper.close();
            reader.close();

            return bos.toByteArray();
        }

    }

    // combines a list of pages together
    private byte[] collectPages(List<Template> pages, Toc tableOfContents, Contact contact, String sortPrefix) throws IOException, DocumentException {
        byte[] pdf = new byte[]{};
        int i = 1;
        for ( Template pdfTemplate : pages ) {
            if ( ! pdfTemplate.isTocTemplate() ) {
                byte[] template = templateRepository.findOne(pdfTemplate.getUrl());
                if ( pdfTemplate.isPersonalisation() ) {
                    template = personalize(template, contact);
                }
                pdf = iTextUtil.merge(pdf, template);
                tableOfContents.addEntries(1, Arrays.asList(pdfTemplate.getName()), template, pdfTemplate.isToc(), sortPrefix + "___" + (i++) );
            }
        }
        return pdf;
    }

    private PdfPTable createModelPage(Contact contact, Model model, PdfWriter pdfWriter) throws IOException, DocumentException {

        final PdfPTable pdfPTable = new PdfPTable( new float[] { 40f, 1f, 59f } );
        pdfPTable.setTotalWidth(iTextUtil.PAGE_SIZE.getWidth());
        pdfPTable.addCell(cell(buildModelDetailSection(model, pdfWriter)));
        pdfPTable.addCell(cell(new Paragraph()));
        pdfPTable.addCell(cell(buildModelPricingTables(contact, model)));
        return pdfPTable;

    }

    private Phrase processHtmlCodes(String name, Font baseFont, Font symbol) {

        final Font italicFont = new Font(baseFont);
        italicFont.setStyle(Font.FontStyle.ITALIC.getValue());

        final Font normalFont = new Font(baseFont);

        Font usedFont = normalFont;

        final Phrase phrase = new Phrase();

        if ( ! StringUtils.isEmpty( name ) ) {

            for ( String[] alphabet : GreekAlphabet.getAlphabet() ) {
                name = name.replaceAll(alphabet[0], DELIMETER + alphabet[0] + DELIMETER );
            }
            name = name.replaceAll("<sup>|<SUP>", DELIMETER + "<sup>" );
            name = name.replaceAll("</sup>|</SUP>", DELIMETER );
            name = name.replaceAll("<i>|<I>|<em>|<EM>", DELIMETER + "<i>" );
            name = name.replaceAll("</i>|</I>|</em>|</EM>", DELIMETER + "</i>" );

            final String[] tokens = name.split(DELIMETER);
            for ( String token : tokens ) {

                String text = token;
                if ( text.startsWith("<i>") ) {
                    usedFont = italicFont;
                    text = text.substring(3);
                } else if ( text.startsWith("</i>") ) {
                    usedFont = normalFont;
                    text = text.substring(4);
                }

                usedFont.setSize(baseFont.getSize());

                if ( text.startsWith("&") ) {
                    final char replacement = GreekAlphabet.getReplacement(text);
                    if (!Character.isWhitespace(replacement)) {
                        phrase.add(SpecialSymbol.get(replacement, symbol));
                    } else {
                        phrase.add(new Chunk(text, usedFont));
                    }
                } else if ( text.startsWith("<sup>") ) {

                    final Font superScriptFont = new Font(usedFont);
                    superScriptFont.setSize(baseFont.getSize() - 1.5f);

                    final Chunk superScript = new Chunk(text.substring(5), superScriptFont);
                    superScript.setTextRise(4f);
                    phrase.add(superScript);

                } else {
                    phrase.add ( new Chunk ( text, usedFont ) );
                }

            }
        }

        return phrase;
    }

    private PdfPTable buildModelPricingTables(Contact contact, Model model) {

        final PdfPTable table = new PdfPTable(1);

        final List<Pricing> validPricings = model.validPricings ( contact );

        if ( CollectionUtils.isEmpty( validPricings )) {
            final Chunk chunk = new Chunk("Contact us for pricing on this model", iTextUtil.getFontContactUs());
            chunk.setAction(new PdfAction(contactUsUrl));
            table.addCell(cell(new Phrase(chunk)));
        } else {
            for ( Pricing pricing : validPricings ) {
                table.addCell ( cell ( buildModelPricingTable(pricing) ) );
            }
        }

        return table;

    }

    private PdfPTable buildModelPricingTable(Pricing pricing) {
        final PdfPTable pricingTable = new PdfPTable(pricing.getNumberOfHeaderItems());

        pricingTable.addCell(cell(new Paragraph(pricing.getCategory(), iTextUtil.getFontModelCategory()), pricing.getNumberOfHeaderItems()));
        pricingTable.addCell(cell(new Paragraph(" "), pricing.getNumberOfHeaderItems()));

        if ( pricing.isQuantities() ) {
            pricingTable.addCell(cellH(new Paragraph("Quantity", iTextUtil.getFontModelPricingTitle())));
        }
        if ( pricing.isAge() ) {
            pricingTable.addCell(cellH(new Paragraph("Age (weeks)", iTextUtil.getFontModelPricingTitle())));
        }
        if ( pricing.isMale() ) {
            pricingTable.addCell(cellH(new Paragraph("Male", iTextUtil.getFontModelPricingTitle())));
        }
        if ( pricing.isFemale() ) {
            pricingTable.addCell(cellH(new Paragraph("Female", iTextUtil.getFontModelPricingTitle())));
        }
        boolean invert = false;
        for ( Line line : pricing.getLines() ) {
            if ( pricing.isQuantities() ) {
                pricingTable.addCell(cellD(new Paragraph(line.getQuantity(), iTextUtil.getFontModelPricingData()), invert));
            }
            if ( pricing.isAge() ) {
                pricingTable.addCell(cellD(new Paragraph(line.getAge(), iTextUtil.getFontModelPricingData()), invert));
            }
            if ( pricing.isMale() ) {
                pricingTable.addCell(cellD(new Paragraph(line.getMale(), iTextUtil.getFontModelPricingData()), invert));
            }
            if ( pricing.isFemale() ) {
                pricingTable.addCell(cellD(new Paragraph(line.getFemale(), iTextUtil.getFontModelPricingData()), invert));
            }
            invert = !invert;
        }

        /*
        final PdfPCell cell = new PdfPCell(pricingTable);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.DARK_GRAY);
        table.addCell(cell);
        */

        if ( ! StringUtils.isEmpty(pricing.getMessage())) {
            pricingTable.addCell(cell(new Paragraph (pricing.getMessage(), iTextUtil.getFontModelPricingMessage()), pricing.getNumberOfHeaderItems()));
        }
        pricingTable.addCell(cell(new Paragraph(" "), pricing.getNumberOfHeaderItems()));

        return pricingTable;
    }

    private PdfPTable buildModelDetailSection(Model model, PdfWriter pdfWriter) throws IOException, BadElementException {

        final Phrase p = processHtmlCodes(model.getProductNameProcessed(), iTextUtil.getFontModelTitle(), iTextUtil.getFontModelSymbol());
        for ( Chunk c : p.getChunks() ) {
            c.setAction(new PdfAction(model.getUrl()));
        }

        final StringBuilder strAppList = new StringBuilder();
        for ( String application : model.getApplicationsSorted() ) {
            if ( strAppList.length() != 0 ) {
                strAppList.append(", ");
            }
            strAppList.append(application);
        }

        final PdfPTable table = new PdfPTable(1);

        table.addCell(cell(p));

        table.addCell(createRow("Model Number", model.getModelNumber(), null));
        table.addCell(createRow("Animal Type", model.getAnimalType(), null));
        table.addCell(createRow("Nomenclature", model.getNomenclatureParsed(), null));
        table.addCell(createRow("Application(s)", strAppList.toString(), null));
        table.addCell(createRow("Health Report", model.getHealthReport(), model.getHealthReport()));
        table.addCell(createRow("Species", model.getSpecies(), null));

        table.addCell(cell(createOrderButton(model, pdfWriter)) );

        return table;

    }

    private PdfPTable createOrderButton(Model model, PdfWriter pdfWriter) throws IOException, BadElementException {

        final Chunk chunk = new Chunk ( "Order on taconic.com", iTextUtil.getFontButton() );
        chunk.setAction(new PdfAction("http://www.taconic.com/start-an-order?modelNumber=" + model.getModelNumber()));

        final PdfPCell cell = cell(new Phrase(chunk));
        cell.setBackgroundColor(iTextUtil.getTaconicRed());
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

        if ( value == null ) {
            value = "";
        }

        final Phrase valuePhrase = processHtmlCodes(value.trim(), iTextUtil.getFontModelValue(), iTextUtil.getFontModelSymbol());
        if ( ! StringUtils.isEmpty(url)) {
            for ( Chunk chunk : valuePhrase.getChunks() ) {
                chunk.setAction(new PdfAction(url));
            }
        }
        for ( Chunk chunk : valuePhrase.getChunks() ) {
            chunk.setLineHeight(13f);
        }
        paragraph.add(valuePhrase);

        final PdfPCell cell = cell(paragraph);
        cell.setPaddingBottom(5f);
        return cell;
    }

    private PdfPCell cell ( Phrase p, int colSpan ) {
        final PdfPCell cell = cell(p);
        cell.setColspan(colSpan);
        return cell;
    }

    private PdfPCell cell ( Phrase p ) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell cellD ( Phrase p, boolean invert ) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if ( invert ) {
            cell.setBackgroundColor(iTextUtil.getSilver());
        }
        return cell;
    }

    private PdfPCell cellH ( Phrase p ) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(iTextUtil.getPurple());
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell cell ( PdfPTable p ) {
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorder(0);
        return cell;
    }

    private byte[] personalize(byte[] pdf, Contact contact) throws IOException, DocumentException {

        try ( final ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);
            final PdfContentByte text = stamper.getOverContent(1);

            text.beginText();

            text.setColorFill(iTextUtil.getFontPersonalization().getColor());
            text.setFontAndSize(iTextUtil.getFontPersonalization().getBaseFont(), iTextUtil.getFontPersonalization().getSize());
            text.showTextAligned(Element.ALIGN_CENTER, contact.getFullName(), 305, 630, 0);

            // set company name
            if (!StringUtils.isEmpty(contact.getCompany())) {
                text.showTextAligned(Element.ALIGN_CENTER, contact.getCompany(), 305, 613, 0);
            }
            text.showTextAligned(Element.ALIGN_CENTER, new SimpleDateFormat("MM-dd-yyyy").format(new Date()), 305, 594, 0);

            text.endText();

            stamper.close();
            reader.close();
            return bos.toByteArray();

        }

    }

    private byte[] createBookmarks(byte[] pdf, Toc tableOfContents) throws DocumentException, IOException {

        // create the bookmarks
        final List<HashMap<String, Object>> outlines = new ArrayList<>();

        final List<TocEntry> entriesSorted = tableOfContents.getEntriesSorted();
        int pageNumber = 1;
        int originalPageNumber = entriesSorted.get(0).getOriginalPageNumber();
        HashMap<String, Object> modelBookmark;
        final List<HashMap<String, Object>> modelBookmarkKids = new ArrayList<>();

        for ( TocEntry tocEntry : entriesSorted) {

            if ( originalPageNumber != tocEntry.getOriginalPageNumber() ) {
                pageNumber += tocEntry.getNumberOfPages();
                originalPageNumber = tocEntry.getOriginalPageNumber();
            }

            if ( tocEntry.isIncludedInToc() ) {

                final HashMap<String, Object> bookmark = new HashMap<>();

                String name = tocEntry.getName();
                name = name.replaceAll("<sup>", "");
                name = name.replaceAll("</sup>", "");
                name = name.replaceAll("<i.*?>", "");
                name = name.replaceAll("</i>", "");
                name = GreekAlphabet.replaceGreekHtmlCodesWithUnicode(name);

                bookmark.put("Title", name);
                bookmark.put("Action", "GoTo");
                bookmark.put("Page", String.format("%d Fit", pageNumber - tocEntry.getNumberOfPages() + 1));
                if ( tocEntry.getLevel() == 1 ) {
                    outlines.add(bookmark);
                } else {
                    modelBookmarkKids.add(bookmark);
                }

                if ( tocEntry.isModelHeader() ) {
                    modelBookmark = bookmark;
                    modelBookmark.put("Open", true);
                    modelBookmark.put("Kids", modelBookmarkKids);
                }

            }

        }

        try ( final ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {
            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            stamper.setOutlines(outlines);
            stamper.close();
            reader.close();

            return bos.toByteArray();
        }

    }

    private byte[] stampTableOfContents(byte[] pdf, Toc tableOfContents) throws IOException, DocumentException {

        try ( final ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfStamper stamper = new PdfStamper(reader, bos);

            // stamp the named destinations
            for ( int pageNumber = 1; pageNumber <= reader.getNumberOfPages(); pageNumber++ ) {
                stamper.addNamedDestination("page"+pageNumber, pageNumber, new PdfDestination(PdfDestination.XYZ, 80f, 800f, 0));
            }

            // create the table of contents
            final Chunk tocTitle = new Chunk(" TABLE OF CONTENTS", iTextUtil.getFontTocTitle());

            int currentTocPage = tableOfContents.getFirstPageOfToc();
            PdfContentByte canvas = stamper.getOverContent(currentTocPage);

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(tocTitle), 55, 700, 0);

            final List<TocEntry> entriesSorted = tableOfContents.getEntriesSorted();
            int tocEntryNumber = 0;
            int pageNumber = 1;
            int originalPageNumber = entriesSorted.get(0).getOriginalPageNumber();
            for (TocEntry tocEntry : entriesSorted) {

                if ( originalPageNumber != tocEntry.getOriginalPageNumber() ) {
                    pageNumber += tocEntry.getNumberOfPages();
                    originalPageNumber = tocEntry.getOriginalPageNumber();
                }

                if (tocEntry.isIncludedInToc()) {
                    tocEntryNumber++;

                    int gotoPageNumber = pageNumber - tocEntry.getNumberOfPages() + 1;

                    // take the right TOC page to stamp the TOC entry on (needed for TOC's with multiple pages)
                    if (tocEntryNumber % NUMBER_OF_ITEMS_PER_TOC_PAGE == 0) {
                        currentTocPage++;
                        canvas = stamper.getOverContent(currentTocPage);
                    }

                    Font font = iTextUtil.getFontToc();
                    if ( tocEntry.getLevel() == 1 ) {
                        font = iTextUtil.getFontTocBold();
                    }

                    final Phrase p = processHtmlCodes(tocEntry.getLevelString() + tocEntry.getName(), font, iTextUtil.getFontTocSymbol());
                    p.add(new Chunk(" ", iTextUtil.getFontToc()));
                    if ( tocEntry.isShowingPageNumber() ) {
                        p.add(new Chunk(new DottedLineSeparator()));
                        p.add(new Chunk(" " + String.valueOf(gotoPageNumber), iTextUtil.getFontToc()));
                    }

                    for (Chunk chunk : p.getChunks()) {
                        chunk.setAction(PdfAction.gotoLocalPage("page" + gotoPageNumber, false));
                    }

                    final int y = 680 - (16 * (tocEntryNumber % NUMBER_OF_ITEMS_PER_TOC_PAGE));

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

    private byte[] enableLinkToWebsite(byte[] pdf, Toc tableOfContents) throws IOException, DocumentException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final PdfReader reader = new PdfReader(pdf);

            final PdfStamper stamper = new PdfStamper(reader, bos);

            for ( int i = tableOfContents.getFirstPageOfToc(); i <= tableOfContents.getLastPageNumberOfModelPages(); i++ ) {

                final Chunk websiteChunk = new Chunk("..................");
                websiteChunk.setAction(new PdfAction(websiteLink));

                ColumnText ct = new ColumnText(stamper.getUnderContent(i));
                ct.setSimpleColumn(335, 10, 400, 35);
                ct.addText(new Phrase(websiteChunk));
                ct.go();

                final Chunk emailChunk = new Chunk(".........................................");
                emailChunk.setAction(new PdfAction(emailLink));

                ct = new ColumnText(stamper.getUnderContent(i));
                ct.setSimpleColumn(240, 10, 330, 35);
                ct.addText(new Phrase(emailChunk));
                ct.go();

            }

            stamper.close();
            reader.close();
            return bos.toByteArray();

        }

    }

}