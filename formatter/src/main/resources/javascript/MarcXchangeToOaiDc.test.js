/** @file Module that contains unit tests for functions in MarcXchangeToOaiDc module */

use( "MarcXchangeToOaiDc" );
use( "UnitTest" );

UnitTest.addFixture( "test createDcXml", function( ) {

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">23645564</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
                '<marcx:subfield code="c">20160525133413</marcx:subfield>' +
                '<marcx:subfield code="d">20010824</marcx:subfield>' +
                '<marcx:subfield code="f">a</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
                '<marcx:subfield code="t">m</marcx:subfield>' +
                '<marcx:subfield code="u">r</marcx:subfield>' +
                '<marcx:subfield code="a">2001</marcx:subfield>' +
                '<marcx:subfield code="z">2003</marcx:subfield>' +
                '<marcx:subfield code="b">dk</marcx:subfield>' +
                '<marcx:subfield code="d">x</marcx:subfield>' +
                '<marcx:subfield code="j">f</marcx:subfield>' +
                '<marcx:subfield code="l">dan</marcx:subfield>' +
                '<marcx:subfield code="v">0</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">a</marcx:subfield>' +
            '<marcx:subfield code="g">xx</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="021">' +
                '<marcx:subfield code="a">87-7724-857-0</marcx:subfield>' +
                '<marcx:subfield code="c">ib.</marcx:subfield>' +
                '<marcx:subfield code="d">kr. 169,95</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="041">' +
                '<marcx:subfield code="a">dan</marcx:subfield>' +
                '<marcx:subfield code="c">jpn</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Murakami</marcx:subfield>' +
            '<marcx:subfield code="h">Haruki</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="241">' +
            '<marcx:subfield code="a">Nejimaki-dori kuronikure</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="245">' +
            '<marcx:subfield code="a">Traekopfuglens kroenike</marcx:subfield>' +
            '<marcx:subfield code="e">Haruki Murakami</marcx:subfield>' +
            '<marcx:subfield code="f">oversat af Mette Holm</marcx:subfield>' +
            '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="260">' +
            '<marcx:subfield code="a">Aarhus</marcx:subfield>' +
            '<marcx:subfield code="b">Klim</marcx:subfield>' +
            '<marcx:subfield code="c">2003</marcx:subfield>' +
            '<marcx:subfield code="k">tr.i udl.</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

//    var expected = XmlUtil.fromString(
//        '<oai_dc:dc ' +
//        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
//        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
//        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
//        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
//            '<dc:title>Trækopfuglens krønike</dc:title>' +
//            '<dc:creator>Haruki Murakami</dc:creator>' +
//            '<dc:publisher>Klim</dc:publisher>' +
//            '<dc:date>2001</dc:date>' +
//            '<dc:identifier>870970,23645564</dc:identifier>' +
//            '<dc:identifier>ISBN:87-7724-857-0</dc:identifier>' +
//            '<dc:source>Nejimaki-dori kuronikure</dc:source>' +
//            '<dc:language>Dansk</dc:language>' +
//        '</oai_dc:dc>'
//    );
    
    var expected = XmlUtil.fromString(
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
            '<dc:title>Traekopfuglens kroenike</dc:title>' +
        '</oai_dc:dc>'
    );

    Assert.equalXml( "createDcXml", MarcXchangeToOaiDc.createDcXml( recordString ), expected );

} );

UnitTest.addFixture( "test addDcTitleElement", function( ) {

    var record = new Record();
    record.fromString(
        '001 00 *a2 364 556 4 *b870970 *c20160525133413 *d20010824 *fa\n' +
        '245 00 *aTraekopfuglens kroenike *eHaruki Murakami *foversat af Mette Holm\n'
    );

    var xml = XmlUtil.fromString(
        '<oai_dc:dc ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd"/>'
    );

    var actual = MarcXchangeToOaiDc.__callElementMethod( MarcXchangeToOaiDc.addDcTitleElement, xml, record );

    var expected = XmlUtil.fromString(
        '<oai_dc:dc ' +
        'xmlns:dc="http://purl.org/dc/elements/1.1/" ' +
        'xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
        'xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">' +
        '<dc:title>Traekopfuglens kroenike</dc:title>' +
        '</oai_dc:dc>'
    );

    Assert.equalXml( "addDcTitleElement", actual, expected );

} );




