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

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class GreekAlphabet {

    /**
     * Based on data provided at http://symbolcodes.tlt.psu.edu/bylanguage/greekchart.html
     */
    public static List<String[]> getAlphabet() {
        return Arrays.asList(
                new String[] { "&alpha;", "a", "\u03B1" },
                new String[] { "&beta;", "b", "\u03B2" },
                new String[] { "&chi;", "c", "\u03C7" },
                new String[] { "&delta;", "d", "\u03B4" },
                new String[] { "&epsilon;", "e", "\u03B5" },
                new String[] { "&gamma;", "g", "\u03B3" },
                new String[] { "&eta;", "h", "\u03B7" },
                new String[] { "&iota;", "i", "\u03B9" },
                new String[] { "&phi;", "j", "\u03C6" },
                new String[] { "&kappa;", "k", "\u03BA" },
                new String[] { "&lambda;", "l", "\u03BB" },
                new String[] { "&mu;", "m", "\u03BC" },
                new String[] { "&nu;", "n", "\u03BD" },
                new String[] { "&omicron;", "o", "\u03BF" },
                new String[] { "&pi;", "p", "\u03C0" },
                new String[] { "&theta;", "q", "\u03B8" },
                new String[] { "&rho;", "r", "\u03C1" },
                new String[] { "&sigma;", "s", "\u03C3" },
                new String[] { "&tau;", "t", "\u03C4" },
                new String[] { "&upsilon;", "u", "\u03C5" },
                new String[] { "&omega;", "w", "\u03C9" },
                new String[] { "&xi;", "x", "\u03BE" },
                new String[] { "&psi;", "y", "\u03C8" },
                new String[] { "&zeta;", "z", "\u03B6" }
        );

    }

    public static char getReplacement ( String greekSymbol ) {

        for ( String[] letter : getAlphabet() ) {
            if ( greekSymbol.equals(letter[0]) ) {
                return letter[1].toCharArray()[0];
            }
        }

        return ' ';

    }

    public static String replaceGreekHtmlCodesWithUnicode(String text) {
        if ( !StringUtils.isEmpty(text)) {
            for ( String[] greekLetter : getAlphabet() ) {
                text = text.replaceAll(greekLetter[0], greekLetter[2]);
            }
        }
        return text;
    }

}