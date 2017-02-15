/** @file Module that contains unit tests for functions in MarcXchangeToOaiMarcX module */

use( "MarcXchangeToOaiMarcX" );
use( "UnitTest" );

UnitTest.addFixture( "Test", function( ) {

    var recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">20049278</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
                '<marcx:subfield code="a">1992</marcx:subfield>' +
                '<marcx:subfield code="z">2016</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">a</marcx:subfield>' +
                '<marcx:subfield code="g">xx</marcx:subfield>' +
                '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="a">DBF201709</marcx:subfield>' +
                '<marcx:subfield code="x">BKM201709</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Madsen</marcx:subfield>' +
                '<marcx:subfield code="h">Peter</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Frejas smykke</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="300">' +
                '<marcx:subfield code="a">48 sider</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="440">' +
                '<marcx:subfield code="0"/>' +
                '<marcx:subfield code="a">Valhalla</marcx:subfield>' +
                '<marcx:subfield code="v">8</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="504">' +
                '<marcx:subfield code="&#38;">1</marcx:subfield>' +  //subfield &
                '<marcx:subfield code="a">Kaerlighedsgudinden Freja er i besiddelse af et kostbart halssmykke</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="520">' +
                '<marcx:subfield code="&#38;">1</marcx:subfield>' +  //subfield &
                '<marcx:subfield code="a">Originaludgave: 1992</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="520">' +
                '<marcx:subfield code="a">Tidligere: 1. udgave. 1992</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="521">' +
                '<marcx:subfield code="b">4. oplag</marcx:subfield><marcx:subfield code="c">2016</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="652">' +
                '<marcx:subfield code="o">sk</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="700">' +
                '<marcx:subfield code="a">Kure</marcx:subfield>' +
                '<marcx:subfield code="h">Henning</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="945">' +
                '<marcx:subfield code="a">Peter Madsens Valhalla</marcx:subfield>' +
                '<marcx:subfield code="z">440(a)</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="996">' +
                '<marcx:subfield code="a">DBC</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var expected = XmlUtil.fromString(
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">20049278</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
                '<marcx:subfield code="a">1992</marcx:subfield>' +
                '<marcx:subfield code="z">2016</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">a</marcx:subfield>' +
                '<marcx:subfield code="g">xx</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="a">DBF201709</marcx:subfield>' +
                '<marcx:subfield code="x">BKM201709</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="100">' +
                '<marcx:subfield code="a">Madsen</marcx:subfield>' +
                '<marcx:subfield code="h">Peter</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Frejas smykke</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="300">' +
                '<marcx:subfield code="a">48 sider</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="440">' +
                '<marcx:subfield code="0"/>' +
                '<marcx:subfield code="a">Valhalla</marcx:subfield>' +
                '<marcx:subfield code="v">8</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="520">' +
                '<marcx:subfield code="a">Tidligere: 1. udgave. 1992</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="521">' +
                '<marcx:subfield code="b">4. oplag</marcx:subfield>' +
            '<marcx:subfield code="c">2016</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="652">' +
                '<marcx:subfield code="o">sk</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="700">' +
                '<marcx:subfield code="a">Kure</marcx:subfield>' +
                '<marcx:subfield code="h">Henning</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="945">' +
                '<marcx:subfield code="a">Peter Madsens Valhalla</marcx:subfield>' +
                '<marcx:subfield code="z">440(a)</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="996">' +
                '<marcx:subfield code="a">DBC</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    var actual = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( recordString );

    Assert.equalXml( "createMarcXmlWithoutBkmFields (no field 504 or 520 (with subfield &))", actual, expected );


    recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">52331048</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="006">' +
                '<marcx:subfield code="d">11</marcx:subfield>' +
                '<marcx:subfield code="2">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
                '<marcx:subfield code="t">m</marcx:subfield>' +
                '<marcx:subfield code="u">f</marcx:subfield>' +
                '<marcx:subfield code="a">2017</marcx:subfield>' +
                '<marcx:subfield code="b">dk</marcx:subfield>' +
                '<marcx:subfield code="k">b</marcx:subfield>' +
                '<marcx:subfield code="l">per</marcx:subfield>' +
                '<marcx:subfield code="n">a</marcx:subfield>' +
                '<marcx:subfield code="x">06</marcx:subfield>' +
                '<marcx:subfield code="v">0</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">m</marcx:subfield>' +
                '<marcx:subfield code="g">xe</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="x">ACC201707</marcx:subfield>' +
                '<marcx:subfield code="a">DBI201709</marcx:subfield>' +
                '<marcx:subfield code="x">BKM201709</marcx:subfield>' +
                '<marcx:subfield code="x">FSB201709</marcx:subfield>' +
                '<marcx:subfield code="x">FSC201709</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="041">' +
                '<marcx:subfield code="a">per</marcx:subfield>' +
                '<marcx:subfield code="c">dan</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Sonita</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="260">' +
                '<marcx:subfield code="b">[Det Danske Filminstitut]</marcx:subfield>' +
                '<marcx:subfield code="c">[2017]</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="504">' +
                '<marcx:subfield code="&#38;">1</marcx:subfield>' +
                '<marcx:subfield code="a">I Iran lever 15-aarige Sonita som illegal flygtning</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="508">' +
                '<marcx:subfield code="a">Persisk tale</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="517">' +
                '<marcx:subfield code="&#38;">1</marcx:subfield>' +
                '<marcx:subfield code="a">Maerkning: Tilladt for boern over 11 aar</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="600">' +
                '<marcx:subfield code="1"/>' +
                '<marcx:subfield code="a">Alizadeh</marcx:subfield>' +
                '<marcx:subfield code="h">Sonita</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    expected = XmlUtil.fromString(
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
                '<marcx:subfield code="a">52331048</marcx:subfield>' +
                '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
                '<marcx:subfield code="r">n</marcx:subfield>' +
                '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="006">' +
                '<marcx:subfield code="d">11</marcx:subfield>' +
                '<marcx:subfield code="2">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
                '<marcx:subfield code="t">m</marcx:subfield>' +
                '<marcx:subfield code="u">f</marcx:subfield>' +
                '<marcx:subfield code="a">2017</marcx:subfield>' +
                '<marcx:subfield code="b">dk</marcx:subfield>' +
                '<marcx:subfield code="k">b</marcx:subfield>' +
                '<marcx:subfield code="l">per</marcx:subfield>' +
                '<marcx:subfield code="n">a</marcx:subfield>' +
                '<marcx:subfield code="x">06</marcx:subfield>' +
                '<marcx:subfield code="v">0</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">m</marcx:subfield>' +
                '<marcx:subfield code="g">xe</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="x">ACC201707</marcx:subfield>' +
                '<marcx:subfield code="a">DBI201709</marcx:subfield>' +
                '<marcx:subfield code="x">BKM201709</marcx:subfield>' +
                '<marcx:subfield code="x">FSB201709</marcx:subfield>' +
                '<marcx:subfield code="x">FSC201709</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="041">' +
                '<marcx:subfield code="a">per</marcx:subfield>' +
                '<marcx:subfield code="c">dan</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
                '<marcx:subfield code="a">Sonita</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="260">' +
                '<marcx:subfield code="b">[Det Danske Filminstitut]</marcx:subfield>' +
                '<marcx:subfield code="c">[2017]</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="508">' +
                '<marcx:subfield code="a">Persisk tale</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );

    actual = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( recordString );

    Assert.equalXml( "createMarcXmlWithoutBkmFields (no field 504, 517 (with subfield &) and 600)", actual, expected );


    recordString = (
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
            '<marcx:datafield ind1="0" ind2="0" tag="001">' +
            '<marcx:subfield code="a">52714524</marcx:subfield>' +
            '<marcx:subfield code="b">870970</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="004">' +
            '<marcx:subfield code="r">n</marcx:subfield>' +
            '<marcx:subfield code="a">e</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="008">' +
            '<marcx:subfield code="t">m</marcx:subfield>' +
            '<marcx:subfield code="a">2016</marcx:subfield>' +
            '<marcx:subfield code="b">dk</marcx:subfield>' +
            '<marcx:subfield code="l">dan</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
            '<marcx:subfield code="a">a</marcx:subfield>' +
            '<marcx:subfield code="g">xe</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="021">' +
            '<marcx:subfield code="e">9788740037258</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
            '<marcx:subfield code="x">ACC201644</marcx:subfield>' +
            '<marcx:subfield code="a">DBF201650</marcx:subfield>' +
            '<marcx:subfield code="x">BKM201650</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="041">' +
            '<marcx:subfield code="a">dan</marcx:subfield>' +
            '<marcx:subfield code="c">nor</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="245">' +
            '<marcx:subfield code="a">Vores historie</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="504">' +
            '<marcx:subfield code="&#38;">1</marcx:subfield>' +
            '<marcx:subfield code="a">Marcus og Martinus Gunnarsen (f. 2002) er to helt normale norske drenge</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="512">' +
            '<marcx:subfield code="a">Downloades i EPUB-format</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="600">' +
            '<marcx:subfield code="a">Gunnarsen</marcx:subfield>' +
            '<marcx:subfield code="h">Marcus</marcx:subfield>' +
            '<marcx:subfield code="1"/>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="600">' +
            '<marcx:subfield code="a">Gunnarsen</marcx:subfield>' +
            '<marcx:subfield code="h">Martinus</marcx:subfield>' +
            '<marcx:subfield code="1"/>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="610">' +
            '<marcx:subfield code="a">Marcus &amp; Martinus</marcx:subfield>' +
            '<marcx:subfield code="1"/>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="666">' +
            '<marcx:subfield code="0"/>' +
            '<marcx:subfield code="f">sangere</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="666">' +
            '<marcx:subfield code="0"/>' +
            '<marcx:subfield code="e">Norge</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="990">' +
            '<marcx:subfield code="o">201650</marcx:subfield>' +
            '<marcx:subfield code="b">b</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="991">' +
            '<marcx:subfield code="o">Trykt version med lektoerudtalelse (5 272 376 0)</marcx:subfield>' +
            '</marcx:datafield>' +
        '</marcx:record>'
    );


    expected = XmlUtil.fromString(
        '<marcx:record format="danMARC2" type="Bibliographic" xmlns:marcx="info:lc/xmlns/marcxchange-v1">' +
        '<marcx:leader>00000n    2200000   4500</marcx:leader>' +
        '<marcx:datafield ind1="0" ind2="0" tag="001">' +
        '<marcx:subfield code="a">52714524</marcx:subfield>' +
        '<marcx:subfield code="b">870970</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="004">' +
        '<marcx:subfield code="r">n</marcx:subfield>' +
        '<marcx:subfield code="a">e</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="008">' +
        '<marcx:subfield code="t">m</marcx:subfield>' +
        '<marcx:subfield code="a">2016</marcx:subfield>' +
        '<marcx:subfield code="b">dk</marcx:subfield>' +
        '<marcx:subfield code="l">dan</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="009">' +
        '<marcx:subfield code="a">a</marcx:subfield>' +
        '<marcx:subfield code="g">xe</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="021">' +
        '<marcx:subfield code="e">9788740037258</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="032">' +
        '<marcx:subfield code="x">ACC201644</marcx:subfield>' +
        '<marcx:subfield code="a">DBF201650</marcx:subfield>' +
        '<marcx:subfield code="x">BKM201650</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="041">' +
        '<marcx:subfield code="a">dan</marcx:subfield>' +
        '<marcx:subfield code="c">nor</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="245">' +
        '<marcx:subfield code="a">Vores historie</marcx:subfield>' +
        '</marcx:datafield>' +
        '<marcx:datafield ind1="0" ind2="0" tag="512">' +
        '<marcx:subfield code="a">Downloades i EPUB-format</marcx:subfield>' +
        '</marcx:datafield>' +
        '</marcx:record>'
    );

    actual = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( recordString );

    Assert.equalXml( "createMarcXmlWithoutBkmFields (no field 504, 600, 610, 666, 990, 991)", actual, expected );

} );


