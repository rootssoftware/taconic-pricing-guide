package be.roots.taconic.pricingguide.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContactTest {

    @Test
    public void getFullName() {

        final Contact contact = new Contact();

        contact.setFirstName("Koen");

        assertEquals("Koen", contact.getFullName() );

        contact.setLastName("Dehaen");

        assertEquals("Koen Dehaen", contact.getFullName() );

        contact.setSalutation("Dr");

        assertEquals("Dr Koen Dehaen", contact.getFullName() );
    }

    @Test
    public void getEmailGreeting() {

        final Contact contact = new Contact();

        contact.setFirstName("Koen");

        assertEquals("", contact.getEmailGreeting() );

        contact.setLastName("Dehaen");

        assertEquals("Dehaen", contact.getEmailGreeting() );

        contact.setSalutation("Dr");

        assertEquals("Dr Dehaen", contact.getEmailGreeting() );

    }

}