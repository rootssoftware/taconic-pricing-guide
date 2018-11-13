package be.roots.taconic.pricingguide.job;

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

import be.roots.taconic.pricingguide.service.MailService;
import be.roots.taconic.pricingguide.service.ReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;

@Component
public class SendReportJob {

    private final MailService mailService;

    private final ReportService reportService;

    public SendReportJob(MailService mailService, ReportService reportService) {
        this.mailService = mailService;
        this.reportService = reportService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void sendCsvReportOfYesterday() throws MessagingException {

        final byte[] yesterdaysReport = reportService.getLastMonthsReport();

        if ( yesterdaysReport != null ) {

            mailService.sendReport ( reportService.getLastMonth(), reportService.getLastMonthsReportFileName(), yesterdaysReport );
            reportService.archive ( reportService.getLastMonthsReportFileName() );

        }

    }

}