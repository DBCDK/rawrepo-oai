EXPORTED_SYMBOLS = [ ];

use( "UnitTest" );

UnitTest.addFixture( "Dummy test", function( ) {
    Assert.equalValue('Fail this', 'dummy', "dummy");
} );