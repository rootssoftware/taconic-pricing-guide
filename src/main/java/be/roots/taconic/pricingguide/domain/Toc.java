package be.roots.taconic.pricingguide.domain;

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

import be.roots.taconic.pricingguide.service.PDFServiceImpl;
import be.roots.taconic.pricingguide.util.IntUtil;
import be.roots.taconic.pricingguide.util.iTextUtil;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Toc {

    public static final String BEFORE_SORT_PREFIX= "AAA";
    public static final String TOC_SORT_PREFIX = "CCC";
    public static final String MODEL_SORT_PREFIX = "MODEL";
    public static final String AFTER_SORT_PREFIX= "ZZZ";

    private List<TocEntry> entries = new ArrayList<>();
    private int nextPageNumber = 1;

    public List<TocEntry> getEntries() {
        return entries;
    }

    public void addEntries ( int level, List<String> pageNames, byte[] pages, boolean includedInToc, String sort ) throws IOException {
        int numberOfPages = iTextUtil.numberOfPages(pages);
        if ( pages == null ) {
            numberOfPages = 1;
        }

        int i = 0;
        for ( String pageName : pageNames ) {
            entries.add (new TocEntry(level, pageName, includedInToc, nextPageNumber, numberOfPages, sort + "___" + IntUtil.format(i++)));
        }
        if ( pages != null ) {
            nextPageNumber += numberOfPages;
        }
    }

    public void addTocEntry( int numberOfPages ) {
        getEntries().add(new TocEntry( 1, "Table of contents", false, nextPageNumber, numberOfPages, TOC_SORT_PREFIX + "___Table of contents"));
        nextPageNumber += numberOfPages;
    }

    public String getPageSequence() {

        String pageSequence = "";
        for ( TocEntry entry : getEntriesSorted() ) {
            if ( entry.getNumberOfPages() > 0 ) {
                if (! StringUtils.isEmpty(pageSequence)) {
                    pageSequence += ",";
                }
                if ( entry.getNumberOfPages() == 1 ) {
                    pageSequence += entry.getOriginalPageNumber();
                } else {
                    pageSequence += entry.getOriginalPageNumber() + "-" + (entry.getOriginalPageNumber() + entry.getNumberOfPages() - 1);
                }
            }
        }

        return pageSequence;

    }

    public List<TocEntry> getTocEntries() {
        final List<TocEntry> contents = new ArrayList<>();
        for ( TocEntry entry : entries ) {
            if ( entry.isIncludedInToc() ) {
                contents.add(entry);
            }
        }
        return contents;

    }

    public int getNumberOfPages() {
        return (this.getTocEntries().size() / PDFServiceImpl.NUMBER_OF_ITEMS_PER_TOC_PAGE) + 1;
    }

    public List<TocEntry> getEntriesSorted() {
        final List<TocEntry> contents = new ArrayList<>(entries);
        Collections.sort ( contents );
        return contents;
    }

    public int getFirstPageOfToc() {
        final List<TocEntry> contents = getEntriesSorted();
        int pageNumber = 1;
        for ( TocEntry tocEntry : contents ) {
            if ( tocEntry.getSort().startsWith(TOC_SORT_PREFIX)) {
                return pageNumber;
            }
            pageNumber+= tocEntry.getNumberOfPages();
        }

        return -1;
    }

    public int getLastPageNumberOfModelPages() {
        final List<TocEntry> contents = getEntriesSorted();
        int pageNumber = 1;
        int oldPageNumber = contents.get(0).getOriginalPageNumber();
        for ( TocEntry tocEntry : contents ) {
            if ( tocEntry.getSort().startsWith(AFTER_SORT_PREFIX)) {
                return pageNumber - 1;
            }
            if ( oldPageNumber != tocEntry.getOriginalPageNumber() ) {
                pageNumber+= tocEntry.getNumberOfPages();
                oldPageNumber = tocEntry.getOriginalPageNumber();
            }
        }

        return -1;
    }

}