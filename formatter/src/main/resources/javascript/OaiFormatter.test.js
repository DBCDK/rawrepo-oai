/** @file Module that contains unit tests for functions in OaiFormatter module */

use( "OaiFormatter" );
use( "UnitTest" );

UnitTest.addFixture( "Test formatRecords (format DC)", function() {

    var format = 'oai_dc'; //applies to all tests in this Fixture
    var allowedSets = [ "BKM", "NAT" ]; //applies to all tests in this Fixture

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23645564</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                '<marcx:subfield code="h">Haruki</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var records = [ recordString ];

    var expected = (
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
            '<dc:creator>Haruki Murakami</dc:creator>' +
            '<dc:identifier>870970,23645564</dc:identifier>' +
            '<dc:title>Traekopfuglens kroenike</dc:title>' +
        '</oai_dc:dc>'
    );

    var actual = OaiFormatter.formatRecords( records, format, allowedSets );

    var testName = "Format one of one record to DC";

    Assert.equalValue( testName, actual, expected );


    var volumeRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">44816687</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                '<marcx:subfield code="a">44783851</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="g">4. bok</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var headRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">44783851</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="a">h</marcx:subfield>' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Knausgård</marcx:subfield>' +
                '<marcx:subfield code="h">Karl Ove</marcx:subfield>' +
                '<marcx:subfield code="4">aut</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Min kamp</marcx:subfield>' +
                '<marcx:subfield code="c">roman</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    records = [ volumeRecordString, headRecordString ];

    expected = (
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
        '<dc:identifier>870970,44816687</dc:identifier>' +
        '<dc:title>4. bok</dc:title>' +
        '</oai_dc:dc>'
    );

    actual = OaiFormatter.formatRecords( records, format, allowedSets );

    testName = "Format one (the first) of two records to DC";

    Assert.equalValue( testName, actual, expected );


    volumeRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
            '<marcx:subfield code="a">23642468</marcx:subfield>' +
            '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
            '<marcx:subfield code="r">n</marcx:subfield>' +
            '<marcx:subfield code="a">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
            '<marcx:subfield code="a">23642433</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
            '<marcx:subfield code="g">2.1</marcx:subfield>' +
            '<marcx:subfield code="a">Intern sikkerhedsdokumentation</marcx:subfield>' +
            '<marcx:subfield code="e">udarbejdet af: Holstberg Management</marcx:subfield>' +
            '<marcx:subfield code="e">forfatter: Anne Gram</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var sectionRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23642433</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">s</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                '<marcx:subfield code="a">23641348</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="n">2</marcx:subfield>' +
                '<marcx:subfield code="o">Intern sikkerhedsdokumentation og -gennemgang</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    headRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
            '<marcx:subfield code="a">23641348</marcx:subfield>' +
            '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
            '<marcx:subfield code="r">n</marcx:subfield>' +
            '<marcx:subfield code="a">h</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
            '<marcx:subfield code="a">Forebyggelse af arbejdsulykker</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    records = [ volumeRecordString, sectionRecordString, headRecordString ];

    expected = (
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
            '<dc:identifier>870970,23642468</dc:identifier>' +
            '<dc:title>Intern sikkerhedsdokumentation. 2.1</dc:title>' +
        '</oai_dc:dc>'
    );

    actual = OaiFormatter.formatRecords( records, format, allowedSets );

    testName = "Format one (the first) of three records to DC";

    Assert.equalValue( testName, actual, expected );

} );


UnitTest.addFixture( "Test formatRecords (format marcx, bkm as allowed set)", function() {

    var format = 'marcx'; //applies to all tests in this Fixture
    var allowedSets = [ "BKM", "NAT" ]; //applies to all tests in this Fixture

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23645564</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                '<marcx:subfield code="h">Haruki</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="504">' +
                '<marcx:subfield code="a">Det japanske samfund og sindets afkroge</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="z99">' +
                '<marcx:subfield code="a">masseret</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var records = [ recordString ];

    var expected = (
        '<marcx:collection xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
            '<marcx:record format="danMARC2" type="Bibliographic">' +
            '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">23645564</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">c</marcx:subfield>' +
                    '<marcx:subfield code="a">e</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                    '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                    '<marcx:subfield code="h">Haruki</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                    '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                    '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="504">' +
                    '<marcx:subfield code="a">Det japanske samfund og sindets afkroge</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
        '</marcx:collection>'
    );

    var actual = OaiFormatter.formatRecords( records, format, allowedSets );

    var testName = "Format single record to marcxchange + bkm allowed set + remove local fields";

    Assert.equalValue( testName, actual, expected );


    var volumeRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">44816687</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                '<marcx:subfield code="a">44783851</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="g">4. bok</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var headRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">44783851</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="a">h</marcx:subfield>' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Knausgård</marcx:subfield>' +
                '<marcx:subfield code="h">Karl Ove</marcx:subfield>' +
                '<marcx:subfield code="4">aut</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Min kamp</marcx:subfield>' +
                '<marcx:subfield code="c">roman</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    records = [ volumeRecordString, headRecordString ];

    expected = (
        '<marcx:collection xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
            '<marcx:record format="danMARC2" type="BibliographicMain">' +
            '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">44783851</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="a">h</marcx:subfield>' +
                    '<marcx:subfield code="r">c</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                    '<marcx:subfield code="a">Knausgård</marcx:subfield>' +
                    '<marcx:subfield code="h">Karl Ove</marcx:subfield>' +
                    '<marcx:subfield code="4">aut</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="a">Min kamp</marcx:subfield>' +
                    '<marcx:subfield code="c">roman</marcx:subfield>' +
                '</marcx:datafield>' +
                '</marcx:record>' +
            '<marcx:record format="danMARC2" type="BibliographicVolume">' +
            '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">44816687</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">c</marcx:subfield>' +
                    '<marcx:subfield code="a">b</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                    '<marcx:subfield code="a">44783851</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="g">4. bok</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
        '</marcx:collection>'
    );

    actual = OaiFormatter.formatRecords( records, format, allowedSets );

    testName = "Format head and volume records to marcxchange - bkm allowed set";

    Assert.equalValue( testName, actual, expected );


    volumeRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23642468</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                '<marcx:subfield code="a">23642433</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="g">2.1</marcx:subfield>' +
                '<marcx:subfield code="a">Intern sikkerhedsdokumentation</marcx:subfield>' +
                '<marcx:subfield code="e">udarbejdet af: Holstberg Management</marcx:subfield>' +
                '<marcx:subfield code="e">forfatter: Anne Gram</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var sectionRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23642433</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">s</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                '<marcx:subfield code="a">23641348</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="n">2</marcx:subfield>' +
                '<marcx:subfield code="o">Intern sikkerhedsdokumentation og -gennemgang</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    headRecordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23641348</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">h</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Forebyggelse af arbejdsulykker</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    records = [ volumeRecordString, sectionRecordString, headRecordString ];

    expected = (
        '<marcx:collection xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
            '<marcx:record format="danMARC2" type="BibliographicMain">' +
            '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">23641348</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">n</marcx:subfield>' +
                    '<marcx:subfield code="a">h</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="a">Forebyggelse af arbejdsulykker</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
            '<marcx:record format="danMARC2" type="BibliographicSection">' +
            '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">23642433</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">n</marcx:subfield>' +
                    '<marcx:subfield code="a">s</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                    '<marcx:subfield code="a">23641348</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="n">2</marcx:subfield>' +
                    '<marcx:subfield code="o">Intern sikkerhedsdokumentation og -gennemgang</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
            '<marcx:record format="danMARC2" type="BibliographicVolume">' +
            '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">23642468</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">n</marcx:subfield>' +
                    '<marcx:subfield code="a">b</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="014">' +
                    '<marcx:subfield code="a">23642433</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="g">2.1</marcx:subfield>' +
                    '<marcx:subfield code="a">Intern sikkerhedsdokumentation</marcx:subfield>' +
                    '<marcx:subfield code="e">udarbejdet af: Holstberg Management</marcx:subfield>' +
                    '<marcx:subfield code="e">forfatter: Anne Gram</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
        '</marcx:collection>'
    );

    actual = OaiFormatter.formatRecords( records, format, allowedSets );

    testName = "Format head, section and volume records to marcxchange - bkm allowed set";

    Assert.equalValue( testName, actual, expected );

} );


UnitTest.addFixture( "Test formatRecords (format marcx, bkm NOT allowed set)", function() {

    var format = 'marcx'; //applies to all tests in this Fixture
    var allowedSets = [ "NAT" ]; //applies to all tests in this Fixture

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23645564</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                '<marcx:subfield code="h">Haruki</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="504">' +
                '<marcx:subfield code="a">Det japanske samfund og sindets afkroge</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="z99">' +
                '<marcx:subfield code="a">masseret</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var records = [ recordString ];

    var expected = (
        '<marcx:collection xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
            '<marcx:record format="danMARC2" type="Bibliographic">' +
            '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
                '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                    '<marcx:subfield code="a">23645564</marcx:subfield>' +
                    '<marcx:subfield code="b">870970</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                    '<marcx:subfield code="r">c</marcx:subfield>' +
                    '<marcx:subfield code="a">e</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                    '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                    '<marcx:subfield code="h">Haruki</marcx:subfield>' +
                '</marcx:datafield>' +
                '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                    '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                    '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                    '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
                '</marcx:datafield>' +
            '</marcx:record>' +
        '</marcx:collection>'
    );

    var actual = OaiFormatter.formatRecords( records, format, allowedSets );

    var testName = "Format single record to marcxchange - BKM not allowed set + remove local fields";

    Assert.equalValue( testName, actual, expected );

} );

UnitTest.addFixture( "Test formatRecords (format marcx, bkm NOT allowed set)", function() {

    var format = 'illegal';
    var allowedSets = [ "BKM", "NAT" ];

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000c    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23645564</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">c</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Murakami</marcx:subfield>' +
                '<marcx:subfield code="h">Haruki</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
                '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
                '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var records = [ recordString ];

    var error = new Error( "Format: illegal not allowed" );

    use( "SafeAssert" );
    SafeAssert.exception( "Throw error when format is not legal", function( ) {
        OaiFormatter.formatRecords( records, format, allowedSets )
    }, error );

} );

UnitTest.addFixture( "Test getAllowedFormats", function() {

    Assert.equalValue( "get allowed formats", OaiFormatter.getAllowedFormats(), [ 'oai_dc', 'marcx' ] );

} );