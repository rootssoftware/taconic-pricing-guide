package be.roots.taconic.pricingguide.domain;

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

import be.roots.taconic.pricingguide.service.PDFServiceImpl;
import be.roots.taconic.pricingguide.util.IntUtil;
import be.roots.taconic.pricingguide.util.iTextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

public class Toc {

    public static final String BEFORE_SORT_PREFIX= "AAA";
    private static final String TOC_SORT_PREFIX = "CCC";
    public static final String MODEL_SORT_PREFIX = "MODEL";
    public static final String AFTER_SORT_PREFIX= "ZZZ";

    private final List<TocEntry> entries = new ArrayList<>();
    private int nextPageNumber = 1;

    private List<TocEntry> getEntries() {
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


        int currentPageNumber = 1;
        int nextPageNumber = 1;

        String pageSequence = "";
        for ( TocEntry entry : getEntriesSorted() ) {

            // setting the correct page number
            if ( entry.isNotSecondItemOnSamePage() ) {
                currentPageNumber = nextPageNumber;
            }
            entry.setFinalPageNumber(currentPageNumber);
            if ( entry.isNotSecondItemOnSamePage() && ! entry.isModelHeader() ) {
                nextPageNumber = currentPageNumber + entry.getNumberOfPages();
            }

            // calculating the pagesequence
            if ( entry.getNumberOfPages() > 0 ) {
                if (hasText(pageSequence)) {
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

    private List<TocEntry> getTocEntries() {
        return entries
                .stream()
                .filter(TocEntry::isIncludedInToc)
                .collect(toList());
    }

    public int getNumberOfPages() {

        final int size = this.getTocEntries().size();
        if ( size <= PDFServiceImpl.getNumberOfItemsPerTocPage(0) ) {
            return 1;
        } else {
            return ((size - PDFServiceImpl.getNumberOfItemsPerTocPage(0)) / PDFServiceImpl.getNumberOfItemsPerTocPage(1)) + 2;
        }
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

}