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

import be.roots.taconic.pricingguide.domain.Contact;
import be.roots.taconic.pricingguide.util.DefaultUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReportServiceImpl.class);

    @Value("${report.location}")
    private String reportLocation;

    @PostConstruct
    public void init() {
        final File directory = new File(reportLocation);
        if ( ! directory.exists() ) {
            directory.mkdirs();
        }
    }

    @Override
    public void report(Contact contact, List<String> modelIds) throws IOException {

        final CSVFormat csvFileFormat = CSVFormat.DEFAULT;
        final FileWriter fileWriter = new FileWriter(getFileNameFor(OffsetDateTime.now()), true);
        final CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        final List<String> record = new ArrayList<>();

        record.add(OffsetDateTime.now().format(DateTimeFormatter.ofPattern(DefaultUtil.FORMAT_TIMESTAMP)));

        record.add(contact.getRemoteIp());
        record.add(contact.getHsId());
        record.add(contact.getSalutation());
        record.add(contact.getFirstName());
        record.add(contact.getLastName());
        record.add(contact.getEmail());
        record.add(contact.getCompany());
        record.add(contact.getCountry());
        record.add(contact.getPersona());
        if ( contact.getJobRole() != null ) {
            record.add(contact.getJobRole().getDescription());
        } else {
            record.add(null);
        }
        record.add(contact.getTherapeuticArea());
        if ( contact.getCurrency() != null ) {
            record.add(contact.getCurrency().name());
            record.add(contact.getCurrency().getDescription());
        } else {
            record.add(null);
            record.add(null);
        }

        record.addAll ( modelIds );

        csvFilePrinter.printRecord(record);
        csvFilePrinter.close();

    }

    @Override
    public OffsetDateTime getLastMonth() {
        return OffsetDateTime.now().minusMonths(1);
    }

    private String getFileNameFor(OffsetDateTime date) {
        return reportLocation + "/report-" + date.format(DateTimeFormatter.ofPattern(DefaultUtil.FORMAT_MONTH))+ ".csv";
    }

    @Override
    public String getLastMonthsReportFileName() {
        return getFileNameFor(getLastMonth());
    }

    @Override
    public byte[] getLastMonthsReport() {
        File file = new File(getLastMonthsReportFileName());
        if(file.exists()) {
            try {
                return IOUtils.toByteArray(new FileInputStream(getLastMonthsReportFileName()));
            } catch (IOException e) {
                LOGGER.error("Couldn't read last months report " + getLastMonthsReportFileName(), e);
            }
        } else {
            LOGGER.info("Last months report does not exist.");
        }
        return null;
    }

    @Override
    public void archive(String lastMonthsReportFileName) {
        final File lastMonthsReportFile = new File(lastMonthsReportFileName);
        if ( lastMonthsReportFile.exists() ) {
            lastMonthsReportFile.renameTo ( new File ( lastMonthsReportFile + ".sent" ) );
        }
    }

}