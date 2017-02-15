/** @file Module that creates marcXchange record for OAI harvested records. */

use( "Log" );
use( "Marc" );
use( "MarcXchange" );

EXPORTED_SYMBOLS = [ 'MarcXchangeToOaiMarcX' ];

/**
 * Module with functions that removes certain fields in marc records
 * for OAI harvest of records from RawRepo.
 *
 * This module contains functions to create modified marcxchange record
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

    /**
     * Function that removes local fields in the marcxchange record
     * (local fields starts with a letter instead of a number) if there
     * are any.
     *
     * @syntax MarcXchangeToOaiMarcX.removeLocalFieldsIfAny( marcXrecord )
     * @param {String} marcXrecord the marcxchange record to check for local fields
     * @return {String} a new marcxchange record without local fields
     * @type {function}
     * @function
     * @name MarcXchangeToOaiDc.removeLocalFieldsIfAny
     */
    function removeLocalFieldsIfAny( marcXrecord ) {

        Log.trace( "Entering MarcXchangeToOaiDc.removeLocalFieldsIfAny" );

        var marcRecord = MarcXchange.marcXchangeToMarcRecord( marcXrecord );
        var modifiedRecord = marcRecord.clone();

        var fieldMatcher = {
            matchField: function( modifiedRecord, field ) {
                //fields starting with a letter should be removed
                return ( field.name.match( /^[a-z]/ ) );
            }
        };
        modifiedRecord.removeWithMatcher( fieldMatcher );
        var marcXml = MarcXchange.marcRecordToMarcXchange( modifiedRecord, "danMARC2", "Bibliographic" );
        var marcXmlString = XmlUtil.toXmlString( marcXml );

        Log.trace( "Leaving MarcXchangeToOaiDc.removeLocalFieldsIfAny" );

        return marcXmlString;

    }


    Log.info( "Leaving MarcXchangeToOaiMarcX module" );

    return {
        createMarcXmlWithoutBkmFields: createMarcXmlWithoutBkmFields,
        removeLocalFieldsIfAny: removeLocalFieldsIfAny
    }

}();