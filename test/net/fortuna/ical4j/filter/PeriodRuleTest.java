/*
 * $Id$
 *
 * Created on 2/02/2006
 *
 * Copyright (c) 2005, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.filter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import junit.framework.TestSuite;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.util.Calendars;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * Unit tests for the period filter rule.
 * @author Ben Fortuna
 */
public class PeriodRuleTest extends FilterTest {
    
    /**
     * @param testMethod
     * @param filter
     * @param collection
     * @param expectedFilteredSize
     */
    public PeriodRuleTest(String testMethod, Filter filter,
            Collection collection, int expectedFilteredSize) {
        super(testMethod, filter, collection, expectedFilteredSize);
    }

    /**
     * @param testMethod
     * @param filter
     * @param collection
     */
    public PeriodRuleTest(String testMethod, Filter filter,
            Collection collection) {
        super(testMethod, filter, collection);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_RELAXED_VALIDATION, false);
    }
    
    /**
     * Test handling of recurrence rules.
     * @throws ParserException
     * @throws IOException
     * @throws ParseException
     */
    /*
    public void testRecurrenceRules() throws ParserException, IOException, ParseException {
        Calendar rCal = Calendars.load("etc/samples/valid/LH1.ics");
        Period period = new Period(new DateTime("20060831T000000Z"), new DateTime("20070831T230000Z")); 
        Filter filter = new Filter(new PeriodRule(period)); 
        Collection tz = rCal.getComponents(Component.VTIMEZONE); 
        Collection zz = filter.filter(rCal.getComponents(Component.VEVENT));
        
        assertTrue(!zz.isEmpty());
//        assertEquals(26, zz.size());
    }
    */
    
    /**
     * @return
     * @throws ParserException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static TestSuite suite() throws FileNotFoundException, IOException, ParserException {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(new FileReader("etc/samples/valid/Australian_TV_Melbourne.ics"));
        
        TestSuite suite = new TestSuite();
        
        //testFilter..
        java.util.Calendar cal = java.util.Calendar.getInstance();
        // April 1, 2004
        cal.set(2004, 3, 1);
        // period of two weeks..
        Period period = new Period(new DateTime(cal.getTime()), new Dur(2));
        Filter filter = new Filter(new PeriodRule(period));
//        ComponentList filtered = (ComponentList) filter.filter(calendar.getComponents());
//        assertTrue(!filtered.isEmpty());
        suite.addTest(new PeriodRuleTest("testFilteredIsNotEmpty", filter, calendar.getComponents()));
        
        //testFilteringAllDayEvents..
        cal = java.util.Calendar.getInstance(); //TimeZone.getTimeZone(TimeZones.GMT_ID));
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 25);
        
        Date start = new Date(cal.getTime());
        
        cal.set(java.util.Calendar.DAY_OF_MONTH, 26);
        Date end = new Date(cal.getTime());
        
        VEvent event = new VEvent(start, end, "mid jan event");
        
        ComponentList components = new ComponentList();
        components.add(event);
        
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.clear(java.util.Calendar.MINUTE);
        cal.clear(java.util.Calendar.SECOND);
        cal.clear(java.util.Calendar.MILLISECOND);
        
        Dur dur = new Dur(1, 0, 0, 0);
        
        while (cal.get(java.util.Calendar.MONTH) == java.util.Calendar.JANUARY) {
            PeriodRule rule = new PeriodRule(new Period(new DateTime(cal.getTime()), dur));
            filter = new Filter(rule);
            if (cal.get(java.util.Calendar.DAY_OF_MONTH) == 25) {
                suite.addTest(new PeriodRuleTest("testFilteredSize", filter, components, 1));
            }
            else {
                suite.addTest(new PeriodRuleTest("testFilteredSize", filter, components, 0));
            }
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        // Test exclusion of particular dates..
        Calendar exCal = Calendars.load("etc/samples/valid/friday13.ics");
        cal = java.util.Calendar.getInstance();
        cal.set(1997, 8, 2, 9, 0, 0);
        DateTime startDt = new DateTime(cal.getTime());
        period = new Period(startDt, new Dur(1));
        filter = new Filter(new PeriodRule(period));
        suite.addTest(new PeriodRuleTest("testFilteredIsEmpty", filter, exCal.getComponents()));
        
        // Test exclusion of particular date patterns..
        exCal = Calendars.load("etc/samples/valid/friday13-NOT.ics");
        cal = java.util.Calendar.getInstance();
        cal.set(1997, 8, 2, 9, 0, 0);
        startDt = new DateTime(cal.getTime());
        period = new Period(startDt, new Dur(52));
        filter = new Filter(new PeriodRule(period));
        suite.addTest(new PeriodRuleTest("testFilteredIsNotEmpty", filter, exCal.getComponents()));
        
        return suite;
    }
}
