/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 * See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

/* global Assert, OaiSetMatcher, UnitTest */

/** @file Module that contains unit tests for functions in OaiSetMatcher module */

use( "OaiSetMatcher" );
use( "UnitTest" );

UnitTest.addFixture( "test OaiSetMatcher.getOaiSets", function( ) {

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
            '<marcx:datafield ind1="0" ind2="0" tag="009">' +
                '<marcx:subfield code="a">a</marcx:subfield>' +
            '<marcx:subfield code="g">xx</marcx:subfield>' +
            '</marcx:datafield>' +
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="a">DBF200338</marcx:subfield>' +
                '<marcx:subfield code="x">SFD200338</marcx:subfield>' +
                '<marcx:subfield code="x">ACC200134</marcx:subfield>' +
                '<marcx:subfield code="x">ACC200332</marcx:subfield>' +
                '<marcx:subfield code="x">DAT201623</marcx:subfield>' +
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

    var expected = [ "NAT", "BKM" ];

    var actual = OaiSetMatcher.getOaiSets( 870970, recordString );

    Assert.equalValue( "Record is contained in NAT and BKM", actual, expected );

} );

UnitTest.addFixture( "test OaiSetMatcher.checkNAT (record in NAT)", function( ) {

    var record = new Record( );
    record.fromString(
        '001 00 *a23645564 *b870970\n' +
        '032 00 *aDBF200338 *xSFD200338 *xACC200134 *xACC200332 *xDAT201623\n'
    );

    var expected = [ "NAT" ];

    var actual = OaiSetMatcher.__callElementMethod( OaiSetMatcher.checkNAT, 870970, record );

    Assert.equalValue( "Record is contained in NAT set", actual, expected );

} );

UnitTest.addFixture( "test OaiSetMatcher.checkNAT (record not in NAT)", function( ) {

    var record = new Record( );
    record.fromString(
        '001 00 *a23645564 *b870970\n' +
        '032 00 *xSFD200338 *xACC200134 *xACC200332 *xDAT201623\n'
    );

    var expected = [ ];

    var actual = OaiSetMatcher.__callElementMethod( OaiSetMatcher.checkNAT, 870970, record );

    Assert.equalValue( "Record is NOT contained in NAT set", actual, expected );

} );

// Helper method for running methods of OaiSetMatcher with multiple values
function parameterized( description, method, agency, values, field, subfield, expected ) {

    var record;
    var actual;
    
    var i;
    for ( i = 0; i < values.length; i++ ) {
        record = new Record();
        record.fromString( field + ' 00 *' + subfield + values[ i ] );

        actual = OaiSetMatcher.__callElementMethod( method, agency, record );

        Assert.equalValue( description + " ( " + field + subfield + "=" + values[ i ] + " )", actual, expected );
    }
}

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record in BKM)", function( ) {

    parameterized( "Record is contained in BKM set",        // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870970,                                  // Agency
                   [ "BKM201347", "BKR201703",              // Run method for each of these 032x value
                     "BKX201604", "ACC201345",
                     "ACM201345", "ACT201345",
                     "SFA200739", "SFD200749",
                     "SFG200727", "SFM199729",
                     "SFU198036", "INV199935",
                     "UTI200449", "NET200045" ],
                   '032',                                   // Field
                   'x',                                     // Subfield
                   [ "BKM" ] );                             // Expected

} );

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record not in BKM - wrong agency)", function( ) {

    parameterized( "Record is not contained in BKM set",    // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870971,                                  // Agency
                   [ "BKM201347", "ACC201345",              // Run method for each of these 032x values
                     "ACT201345", "SFA200739",
                     "SFD200749" ],
                   '032',                                   // Field
                   'x',                                     // Subfield
                   [ ] );                                   // Expected

} );

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record not in BKM)", function( ) {

    parameterized( "Record is not contained in BKM set",    // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870970,                                  // Agency
                   [ "BKI200723", "BDI201114",              // Run method for each of these 032x values
                     "DGI201001", "EBS199043" ],
                   '032',                                   // Field
                   'x',                                     // Subfield
                   [ ] );                                   // Expected

} );




