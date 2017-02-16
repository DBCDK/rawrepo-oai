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
            '<marcx:datafield ind1="0" ind2="0" tag="032">' +
                '<marcx:subfield code="a">dan</marcx:subfield>' +
                '<marcx:subfield code="a">bla</marcx:subfield>' +
                '<marcx:subfield code="x">acc</marcx:subfield>' +
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

    var expected = [ "NAT", "BKM" ];

    Assert.equalValue( "Record is contained in NAT and BKM", OaiSetMatcher.getOaiSets( 870970, recordString ), expected );

} );

// Helper method for running methods of OaiSetMatcher with multiple values
function parameterized( description, method, agency, values, field, subfield, expected ){
    var record;
    var actual;
    
    var i;
    for( i = 0; i < values.length; i++ ) {
        record = new Record();
        record.fromString( field + ' 00 *' + subfield + values[ i ] );

        actual = OaiSetMatcher.__callElementMethod( method, agency, record );

        Assert.equalValue( description + " ( " + field + subfield + "=" + values[ i ] + " )", actual, expected );
    }
}
UnitTest.addFixture( "test OaiSetMatcher.checkNAT (record in NAT)", function( ) {
    
    parameterized( "Record is contained in NAT set",        // Test description
                   OaiSetMatcher.checkNAT,                  // Method to run
                   870970,                                  // Agency
                   [ 'DAN', 'DAR', 'DBF', 'DBI',            // Run method for each of these 032a values 
                     'DLF', 'DMO', 'GBF', 'GMO', 
                     'DKF', 'DMF', 'DOP', 'DPF', 
                     'DPO', 'FPF', 'GPF', 'KIP', 
                     'FBL' ], 
                   '032',                                   // Field
                   'a',                                     // Subfield 
                   [ "NAT" ] );                             // Expected

} );

UnitTest.addFixture( "test OaiSetMatcher.checkNAT (record not in NAT)", function( ) {
    
    parameterized( "Record is NOT contained in NAT set",    // Test description
                   OaiSetMatcher.checkNAT,                  // Method to run
                   870970,                                  // Agency
                   [ 'DA', 'DDR' ],                         // Run method for each of these 032a values 
                   '032',                                   // Field
                   'a',                                     // Subfield 
                   [ ] );                                   // Expected
                   
} );

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record in BKM)", function( ) {
    
    parameterized( "Record is contained in BKM set",        // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870970,                                  // Agency
                   [ "BKM", "ACA", "ACB", "SFA", "SFB" ],   // Run method for each of these 032x values 
                   '032',                                   // Field
                   'x',                                     // Subfield 
                   [ "BKM" ] );                             // Expected
    
} );

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record not in BKM - wrong agency)", function( ) {
    
    parameterized( "Record is not contained in BKM set",    // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870971,                                  // Agency
                   [ "BKM", "ACA", "ACB", "SFA", "SFB" ],   // Run method for each of these 032x values 
                   '032',                                   // Field
                   'x',                                     // Subfield 
                   [ ] );                                   // Expected
    
} );

UnitTest.addFixture( "test OaiSetMatcher.checkBKM (record not in BKM)", function( ) {
    
    parameterized( "Record is not contained in BKM set",    // Test description
                   OaiSetMatcher.checkBKM,                  // Method to run
                   870970,                                  // Agency
                   [ "BKI", "AC", "SF", "BFO" ],            // Run method for each of these 032x values 
                   '032',                                   // Field
                   'x',                                     // Subfield 
                   [ ] );                                   // Expected

} );




