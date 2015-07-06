package be.roots.taconic.pricingguide.util;

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

import be.roots.taconic.pricingguide.domain.Toc;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class iTextUtil {

    private final static Logger LOGGER = Logger.getLogger(iTextUtil.class);

    public static final int PAGE_MARGIN_TOP = 72;
    public static final Rectangle PAGE_SIZE = PageSize.LETTER_LANDSCAPE;
    public static final int PAGE_HEIGHT = (int) PAGE_SIZE.getHeight() - ( PAGE_MARGIN_TOP * 2 ) - 20;

    private static final Font FONT_PERSONALIZATION;

    private static final Font FONT_TOC_TITLE;
    private static final Font FONT_TOC;
    private static final Font FONT_TOC_BOLD;
    private static final Font FONT_TOC_SYMBOL;

    private static final Font FONT_PAGE_NUMBER;

    private static final Font FONT_MODEL_TITLE;
    private static final Font FONT_MODEL_SYMBOL;
    private static final Font FONT_MODEL_CATEGORY;
    private static final Font FONT_MODEL_KEY;
    private static final Font FONT_MODEL_VALUE;
    private static final Font FONT_MODEL_PRICING_TITLE;
    private static final Font FONT_MODEL_PRICING_DATA;
    private static final Font FONT_MODEL_PRICING_MESSAGE;
    private static final Font FONT_CONTACT_US;
    private static final Font FONT_BUTTON;

    private static final BaseColor PURPLE;
    private static final BaseColor SILVER;
    private static final BaseColor TACONIC_RED;

    static {

        // embed the taconic font in the PDF
        /*
        FontFactory.defaultEmbedding = true;
        FontFactory.register("GothamSSm-Medium.otf");
        FontFactory.register("GothamSSm-Light.otf");
*/
        PURPLE = new BaseColor(93, 32, 91);
        SILVER = new BaseColor(239, 239, 239);
        TACONIC_RED = new BaseColor(237, 32, 40);

        BaseFont lightFont = null;
        BaseFont mediumFont = null;

        try {

            lightFont = BaseFont.createFont("GothamSSm-Light.otf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            lightFont.setSubset(false);
            mediumFont = BaseFont.createFont("GothamSSm-Medium.otf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            lightFont.setSubset(false);

        } catch (DocumentException | IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        FONT_PERSONALIZATION = new Font ( mediumFont, 12, Font.NORMAL, BaseColor.DARK_GRAY);

        FONT_TOC_TITLE = new Font ( lightFont, 16, Font.NORMAL, BaseColor.DARK_GRAY);
        FONT_TOC = new Font ( lightFont, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        FONT_TOC_BOLD = new Font ( lightFont, 10, Font.BOLD, BaseColor.DARK_GRAY);
        FONT_TOC_SYMBOL = new Font(Font.FontFamily.SYMBOL, 10, Font.NORMAL, BaseColor.DARK_GRAY);

        FONT_PAGE_NUMBER = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);

        FONT_MODEL_TITLE = new Font ( mediumFont, 12, Font.BOLD, PURPLE);
        FONT_MODEL_SYMBOL = new Font(Font.FontFamily.SYMBOL, 12, Font.BOLD, PURPLE);
        FONT_MODEL_CATEGORY = new Font ( mediumFont, 10, Font.BOLD, PURPLE);
        FONT_MODEL_KEY = new Font ( mediumFont, 8, Font.BOLD, BaseColor.DARK_GRAY);
        FONT_MODEL_VALUE = new Font ( lightFont, 8, Font.NORMAL, BaseColor.DARK_GRAY);

        FONT_MODEL_PRICING_TITLE = new Font ( lightFont, 7, Font.BOLD, BaseColor.WHITE);
        FONT_MODEL_PRICING_DATA = new Font ( lightFont, 7, Font.NORMAL, BaseColor.DARK_GRAY);
        FONT_MODEL_PRICING_MESSAGE = new Font ( lightFont, 6, Font.NORMAL, BaseColor.DARK_GRAY);

        FONT_CONTACT_US = new Font ( mediumFont, 12, Font.BOLD, PURPLE);

        FONT_BUTTON = new Font ( mediumFont, 10, Font.BOLD, BaseColor.WHITE);

    }

    public static BaseColor getPurple() {
        return PURPLE;
    }

    public static BaseColor getSilver() {
        return SILVER;
    }

    public static BaseColor getTaconicRed() {
        return TACONIC_RED;
    }

    public static Font getFontTocSymbol() {
        return FONT_TOC_SYMBOL;
    }

    public static Font getFontTocTitle() {
        return FONT_TOC_TITLE;
    }

    public static Font getFontToc() {
        return FONT_TOC;
    }

    public static Font getFontTocBold() {
        return FONT_TOC_BOLD;
    }

    public static Font getFontPersonalization() {
        return FONT_PERSONALIZATION;
    }

    public static Font getFontPageNumber() {
        return FONT_PAGE_NUMBER;
    }

    public static Font getFontModelTitle() {
        return FONT_MODEL_TITLE;
    }

    public static Font getFontModelKey() {
        return FONT_MODEL_KEY;
    }

    public static Font getFontModelValue() {
        return FONT_MODEL_VALUE;
    }

    public static Font getFontModelCategory() {
        return FONT_MODEL_CATEGORY;
    }

    public static Font getFontModelPricingTitle() {
        return FONT_MODEL_PRICING_TITLE;
    }

    public static Font getFontModelPricingData() {
        return FONT_MODEL_PRICING_DATA;
    }

    public static Font getFontModelPricingMessage() {
        return FONT_MODEL_PRICING_MESSAGE;
    }

    public static Font getFontModelSymbol() {
        return FONT_MODEL_SYMBOL;
    }

    public static Font getFontContactUs() {
        return FONT_CONTACT_US;
    }

    public static Font getFontButton() {
        return FONT_BUTTON;
    }

    public static byte[] merge ( byte[] ... pdfAsBytes ) throws DocumentException, IOException {

        try (final ByteArrayOutputStream copyBaos = new ByteArrayOutputStream()) {
            final Document doc = new Document();
            final PdfCopy copy = new PdfSmartCopy(doc, copyBaos);

            doc.open();

            int numberOfPages = 0;
            final java.util.List<HashMap<String, Object>> bookmarks = new ArrayList<>();
            PdfReader pdf = null;
            for (byte[] pdfAsByte : pdfAsBytes) {

                if ( pdfAsByte != null && pdfAsByte.length > 0 ) {
                    pdf = new PdfReader(pdfAsByte);
                    pdf.consolidateNamedDestinations();
                    final List<HashMap<String, Object>> pdfBookmarks = SimpleBookmark.getBookmark(pdf);
                    if (!CollectionUtils.isEmpty(pdfBookmarks)) {
                        SimpleBookmark.shiftPageNumbers(pdfBookmarks, numberOfPages, null);
                        bookmarks.addAll (pdfBookmarks);
                    }

                    for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                        copy.addPage(copy.getImportedPage(pdf, i));
                    }
                    numberOfPages += pdf.getNumberOfPages();
                }

            }
            if ( pdf != null ) {
                SimpleNamedDestination.getNamedDestination(pdf, false);
            }

            if ( ! CollectionUtils.isEmpty(bookmarks)) {
                copy.setOutlines(bookmarks);
            }

            copy.close();
            return copyBaos.toByteArray();
        }
    }

    public static int numberOfPages(byte[] fileToPrint) throws IOException {
        if ( fileToPrint == null ) {
            return 0;
        }

        return new PdfReader(fileToPrint.clone()).getNumberOfPages();
    }

    public static byte[] setPageNumbers(byte[] pdfDocument) throws IOException, DocumentException {

        final int numberOfPages = numberOfPages ( pdfDocument );

        if ( numberOfPages > 1 ) {

            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {

                final PdfReader reader = new PdfReader(pdfDocument);
                final PdfStamper stamper = new PdfStamper(reader, baos);

                for ( int pageNumber = 2; pageNumber <= numberOfPages; pageNumber ++ ) {
                    // get the first page
                    final PdfContentByte canvas = stamper.getOverContent(pageNumber);

                    // stamp the footer on the page
                    final ColumnText ct = new ColumnText(canvas);

                    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase( pageNumber + "", getFontPageNumber() ), 550, 22, 0);
                    ct.go();

                }

                // close out
                stamper.close();
                reader.close();

                return baos.toByteArray();

            }

        }

        return pdfDocument;

    }

    public static byte[] organize(byte[] pdf, Toc tableOfContents) throws IOException, DocumentException {

        try (final ByteArrayOutputStream copyBaos = new ByteArrayOutputStream()) {

            final Document doc = new Document();
            final PdfCopy copy = new PdfSmartCopy(doc, copyBaos);

            final PdfReader reader = new PdfReader(pdf);
            reader.selectPages(tableOfContents.getPageSequence());

            doc.open();

            for ( int i = 1; i <= reader.getNumberOfPages(); i++ ) {
                copy.addPage(copy.getImportedPage(reader, i));
            }

            reader.close();
            copy.close();
            return copyBaos.toByteArray();
        }

    }

    public static byte[] emptyPage() throws DocumentException, IOException {

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final Document document = createNewDocument();

            final PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
            document.open();
            document.newPage();
            pdfWriter.setPageEmpty(false);
            document.close();
            return baos.toByteArray();
        }
    }

    public static Document createNewDocument() {
        final Document document = new Document();
        document.setPageSize(PAGE_SIZE); //new Rectangle(612, 792);
        document.setMargins(-10, -10, PAGE_MARGIN_TOP, PAGE_MARGIN_TOP);
        return document;
    }

    public static Image getImageFromPdf(byte[] pdf) throws IOException, BadElementException {

        try ( final ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {

            final PdfReader reader = new PdfReader(pdf);
            final PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            final ImageRenderListener listener = new ImageRenderListener(bos);

            parser.processContent(1, listener);

            reader.close();

            return Image.getInstance(bos.toByteArray());
        }

    }

    public static byte[] embedFont(byte[] pdf, String fontFileName, String fontName) throws IOException, DocumentException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // the font file
            RandomAccessFile raf = new RandomAccessFile(fontFileName, "r");
            byte fontfile[] = new byte[(int) raf.length()];
            raf.readFully(fontfile);
            raf.close();
            // create a new stream for the font file
            PdfStream stream = new PdfStream(fontfile);
            stream.flateCompress();
            stream.put(PdfName.LENGTH1, new PdfNumber(fontfile.length));
            // create a reader object
            PdfReader reader = new PdfReader(pdf);
            int n = reader.getXrefSize();
            PdfObject object;
            PdfDictionary font;
            PdfStamper stamper = new PdfStamper(reader, baos);
            PdfName fontname = new PdfName(fontName);
            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isDictionary())
                    continue;
                font = (PdfDictionary) object;
                if (PdfName.FONTDESCRIPTOR.equals(font.get(PdfName.TYPE1))
                        && fontname.equals(font.get(PdfName.FONTNAME))) {
                    PdfIndirectObject objref = stamper.getWriter().addToBody(stream);
                    font.put(PdfName.FONTFILE2, objref.getIndirectReference());
                }
            }
            stamper.close();
            reader.close();
            return baos.toByteArray();
        }
    }

}