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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

@Service
public class MailServiceImpl implements MailService {

    private static final String ADDRESS_LINE =
            "Taconic Biosciences, Inc.<br/>" +
            "1 Discovery Drive<br/>" +
            "Suite 304<br/>" +
            "Rensselaer, NY 12144<br/>" +
            "North America +1 888 822-6642<br/>" +
            "Europe +45 70 23 04 05<br/>" +
            "<a href=\"mailto:info@taconic.com\">info@taconic.com</a><br/>" +
            "<a href=\"http://www.taconic.com\">www.taconic.com</a>";

    @Value("${email.test}")
    private String testEmail;

    @Value("${email.bcc}")
    private String bccEmail;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.report.recipient}")
    private String reportRecipientEmail;
    
    @Value("${document.title}")
    private String documentTitle;

    @Value("${document.file.name}")
    private String documentFileName;

    private final JavaMailSender javaMailSender;

    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMail(Contact contact, byte[] pricingGuide) throws MessagingException {

        final MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);

        helper.setFrom(fromEmail);

        if (isEmpty(testEmail)) {
            helper.setTo(contact.getEmail());
        } else {
            helper.setTo(testEmail.split(","));
        }

        if ( hasText(bccEmail)) {
            helper.setBcc(bccEmail.split(","));
        }

        helper.setSubject("Your " + documentTitle);

        final String body =
                "Dear "+ contact.getEmailGreeting() +",<br/>" +
                "<br/>" +
                "Your "+documentTitle+" is attached.<br/>" +
                "<br/>" +
                "Please <a href=\"http://www.taconic.com/customer-service/contact-us\">contact us</a> for any additional information.<br/>" +
                "<br/>" +
                ADDRESS_LINE;

        helper.setText(body, true);

        helper.addAttachment( documentFileName, new ByteArrayResource(pricingGuide));

        javaMailSender.send(helper.getMimeMessage());

    }

    @Override
    public void sendReport(OffsetDateTime lastMonth, String filename, byte[] report) throws MessagingException {

        final MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);

        helper.setFrom(fromEmail);

        if (isEmpty(testEmail)) {
            helper.setTo(reportRecipientEmail.split(","));
        } else {
            helper.setTo(testEmail.split(","));
        }

        if ( hasText(bccEmail)) {
            helper.setBcc(bccEmail.split(","));
        }

        helper.setSubject(documentTitle + " requests for " + lastMonth.format(DateTimeFormatter.ofPattern(DefaultUtil.FORMAT_MONTH)));

        final String body =
                "Dear,<br/>" +
                "<br/>" +
                "Attached you find the overview of " + documentTitle + " requests for " + lastMonth.format(DateTimeFormatter.ofPattern(DefaultUtil.FORMAT_MONTH)) + ".<br/>" +
                "<br/>" +
                ADDRESS_LINE;

        helper.setText(body, true);

        helper.addAttachment( filename, new ByteArrayResource(report));

        javaMailSender.send(helper.getMimeMessage());

    }

}