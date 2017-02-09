package org.grails.plugins.rx.web.sse

import spock.lang.Specification

class SseWriterSpec extends Specification {

    static final char NEW_LINE = '\n'

    void "test write(int)"() {
        setup:
        def sw = new StringWriter()
        def writer = new SseWriter(sw, 'a')

        when:
        writer.write('foo')
        writer.write(NEW_LINE)
        writer.write('foo')

        then:
        sw.toString() == 'foo\na: foo'
    }

    void "test write(cbuf)"() {
        setup:
        def sw = new StringWriter()
        def writer = new SseWriter(sw, prefix)

        when:
        writer.write(input.chars)

        then:
        sw.toString() == output

        where:
        prefix << ['a','b','b']
        input << ['𐐀oo\r\nfo𐐀\nf𐐀o\n', '', '\n\n\r\n\r\n']
        output << ['𐐀oo\r\na: fo𐐀\na: f𐐀o\na: ', '', '\nb: \nb: \r\nb: \r\nb: ']
    }

    void "test write(cbuf, off, len)"() {
        setup:
        def sw = new StringWriter()
        def writer = new SseWriter(sw, prefix)

        when:
        writer.write(input.chars, offset, length)

        then:
        sw.toString() == output

        where:
        prefix << ['a','b','b']
        input << ['-----foo\r\nfoo\nfoo\n', '--------', '\n\n\r\n\r\n----']
        offset << [5, 4, 0]
        length << [13, 0, 6]
        output << ['foo\r\na: foo\na: foo\na: ', '', '\nb: \nb: \r\nb: \r\nb: ']
    }

    void "test write(string)"() {
        setup:
        def sw = new StringWriter()
        def writer = new SseWriter(sw, prefix)

        when:
        writer.write(input)

        then:
        sw.toString() == output

        where:
        prefix << ['a','b','b']
        input << ['𐐀oo\r\nfo𐐀\nf𐐀o\n', '', '\n\n\r\n\r\n']
        output << ['𐐀oo\r\na: fo𐐀\na: f𐐀o\na: ', '', '\nb: \nb: \r\nb: \r\nb: ']
    }

    void "test write(string, off, len)"() {
        setup:
        def sw = new StringWriter()
        def writer = new SseWriter(sw, prefix)

        when:
        writer.write(input, offset, length)

        then:
        sw.toString() == output

        where:
        prefix << ['a','b','b']
        input << ['-----foo\r\nfoo\nfoo\n', '--------', '\n\n\r\n\r\n----']
        offset << [5, 4, 0]
        length << [13, 0, 6]
        output << ['foo\r\na: foo\na: foo\na: ', '', '\nb: \nb: \r\nb: \r\nb: ']
    }
}