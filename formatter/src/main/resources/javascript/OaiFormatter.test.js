/** @file Module that contains unit tests for functions in OaiFormatter module */

use( "OaiFormatter" );
use( "UnitTest" );

UnitTest.addFixture( "Test formatRecords", function() {

} );


UnitTest.addFixture( "Test getAllowedFormats", function() {

    Assert.equalValue( "get allowed formats", OaiFormatter.getAllowedFormats(), [ 'oai_dc', 'marcx' ] );

} );