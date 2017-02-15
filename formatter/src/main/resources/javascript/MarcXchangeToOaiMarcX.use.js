/** @file Module that creates marcXchange record for OAI harvested records. */

use( "Log" );
use( "Marc" );
use( "MarcXchange" );

EXPORTED_SYMBOLS = [ 'MarcXchangeToOaiMarcX' ];

/**
 * Module with functions that creates .... for OAI harvest of records from RawRepo.
 *
 * This module contains functions to create marcxchange record
 *
 * @type {namespace}
 * @namespace
 */

var MarcXchangeToOaiMarcX = function() {

    Log.info( "Entering MarcXchangeToOaiMarcX module" );

    /**
     * Function that is entry to create a marcxchange record with
     * only national bibliographic fields.
     *
     * @syntax MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( marcXrecord )
     * @param {String} marcXrecord the marcxchange record to create modified record from
     * @return {Document} the created marcXchange record without fields that belong to BKM
     * @type {function}
     * @function
     * @name MarcXchangeToOaiDc.createMarcXmlWithoutBkmFields
     */
    function createMarcXmlWithoutBkmFields( marcXrecord ) {

        Log.trace( "Entering MarcXchangeToOaiDc.createMarcXmlWithoutBkmFields" );

        var marcRecord = MarcXchange.marcXchangeToMarcRecord( marcXrecord );

        var modifiedRecord = marcRecord.clone();

        modifiedRecord.removeAll( "504" );
        modifiedRecord.removeAll( "600" );
        modifiedRecord.removeAll( "610" );
        modifiedRecord.removeAll( "666" );
        modifiedRecord.removeAll( "990" );
        modifiedRecord.removeAll( "991" );

        var fieldMatcher = {
            matchField: function( modifiedRecord, field ) {
                //fields 500-599 should be removed if they have subfield & with value 1
                return ( field.name.match( /^5/ ) && "1" === field.getValue( "&" ) );
            }
        };
        modifiedRecord.removeWithMatcher( fieldMatcher );

        var marcXml = MarcXchange.marcRecordToMarcXchange( modifiedRecord, "danMARC2", "Bibliographic" );

        Log.trace( "Leaving MarcXchangeToOaiDc.createMarcXmlWithoutBkmFields" );

        return marcXml;

    }


    Log.info( "Leaving MarcXchangeToOaiMarcX module" );

    return {
        createMarcXmlWithoutBkmFields: createMarcXmlWithoutBkmFields
    }

}();