package be.roots.taconic.pricingguide.util;

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

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class ImageRenderListener implements RenderListener {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImageRenderListener.class);

    private final ByteArrayOutputStream bos;

    public ImageRenderListener(ByteArrayOutputStream bos) {
        this.bos = bos;
    }

    @Override
    public void beginTextBlock() {
    }

    @Override
    public void endTextBlock() {
    }

    @Override
    public void renderImage(ImageRenderInfo renderInfo) {
        try {
            final PdfImageObject image = renderInfo.getImage();
            if (image != null) {
                bos.write(image.getImageAsBytes());
            }
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
    }

}