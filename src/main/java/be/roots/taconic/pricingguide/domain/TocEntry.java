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

import javax.validation.constraints.NotNull;

public class TocEntry implements Comparable<TocEntry> {

    private final int level;
    private final String name;
    private final int numberOfPages;
    private final int originalPageNumber;
    private int finalPageNumber = -1;
    private final String sort;
    private final boolean includedInToc;

    public TocEntry(int level, String name, boolean includedInToc, int originalPageNumber, int numberOfPages, String sort) {
        this.level = level;
        this.name = name;
        this.includedInToc = includedInToc;
        this.numberOfPages = numberOfPages;
        this.originalPageNumber = originalPageNumber;
        this.sort = sort;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public String getSort() {
        return sort;
    }

    public int getOriginalPageNumber() {
        return originalPageNumber;
    }

    @Override
    public int compareTo(@NotNull TocEntry o) {
        return this.getSort().compareToIgnoreCase(o.getSort());
    }

    @Override
    public String toString() {
        return "TocEntry{" +
                "level=" + level +
                ", name='" + name + '\'' +
                ", numberOfPages=" + numberOfPages +
                ", originalPageNumber=" + originalPageNumber +
                ", sort='" + sort + '\'' +
                '}';
    }

    public String getLevelString() {
        String levelString = "";
        for ( int i = 1; i <= getLevel(); i ++ ) {
            levelString += "  ";
        }

        return levelString;
    }

    public boolean isIncludedInToc() {
        return includedInToc;
    }

    public boolean isShowingPageNumber() {
        return ! isModelHeader();
    }

    public boolean isModelHeader() {
        return ( Toc.MODEL_SORT_PREFIX + "___0000000000" ).equals(sort);
    }

    public boolean isNotSecondItemOnSamePage() {
        return ! ( getLevel() > 1 && ! getSort().endsWith("___0000000000") );
    }

    public void setFinalPageNumber(int finalPageNumber) {
        this.finalPageNumber = finalPageNumber;
    }

    public int getFinalPageNumber() {
        return finalPageNumber;
    }
}