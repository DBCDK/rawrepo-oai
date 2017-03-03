/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 * See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */

/* global XmlUtil, MarcXchangeToOaiMarcX, MarcXchangeToOaiDc, Log, XmlNamespaces */

/** @file Module that formats MarcX. */

use( "Log" );
use( "XmlUtil" );
use( "MarcXchangeToOaiDc" );
use( "MarcXchangeToOaiMarcX" );

EXPORTED_SYMBOLS = [ 'OaiFormatter' ];

/**
 * Module with functions that creates simple Dublin Core elements for OAI harvest of records from RawRepo.
 *
 * This module contains functions to create dc record
 *
 * @type {namespace}
 * @namespace
 */

var OaiFormatter = function() {

    Log.info( "Entering OaiFormatter module" );


    /**
     * Formats MarcX records, either producing a Dublin Core record
     * or MarcX record(s)
     *
     * @function
     * @type {function}
     * @param {String[]} records Array consisting of record, and its ancestors (ordered from closest ancestor)
     * @param {String} format The format to return
     * @param {String[]} allowedSets Names of allowed OAI sets
     * @returns {String} DC or MarcX record(s)
     * @name OaiFormatter.formatRecords
     */
    function formatRecords( records, format, allowedSets ) {

        //lowercasing to make matching easier and avoid errors if input changes
        for ( var i = 0; i < allowedSets.length; i++ ) {
            allowedSets[ i ] = allowedSets[ i ].toLowerCase();
        }

        var marcRecords;

        switch( format ) {

            case 'oai_dc':
                marcRecords = OaiFormatter.convertXmlRecordStringsToMarcObjects( records );
                var higherLevelIdentifiers = [ ];
                for ( var k = 0; k < marcRecords.length - 1; k++ ) {
                    var higherLevelId = MarcXchangeToOaiDc.getHigherLevelIdentifier( marcRecords[ k ] );
                    higherLevelIdentifiers.push( higherLevelId );
                }
                // Format the first record (if more records, the first one is volume)
                return XmlUtil.toXmlString( MarcXchangeToOaiDc.createDcXml( marcRecords[0], higherLevelIdentifiers ) );

            case 'marcx':
                marcRecords = OaiFormatter.convertXmlRecordStringsToMarcObjects( records );
                var marcXCollection = XmlUtil.createDocument( "collection", XmlNamespaces.marcx );
                var bkmRecordAllowed = ( allowedSets.indexOf( 'bkm' ) > -1 );
                
                // Traverse from head to volume
                for ( var j = marcRecords.length - 1; j >= 0; j-- ) {
                    var marcRecord = MarcXchangeToOaiMarcX.removeLocalFieldsIfAny( marcRecords[ j ] );
                    if ( bkmRecordAllowed ) {
                        var marcXDoc = MarcXchangeToOaiMarcX.createMarcXmlWithRightRecordType( marcRecord );
                    } else {
                        marcXDoc = MarcXchangeToOaiMarcX.createMarcXmlWithoutBkmFields( marcRecord );
                    }
                    XmlUtil.appendChild( marcXCollection, marcXDoc );
                }                
                
                return XmlUtil.toXmlString( marcXCollection );
            
            default:
                throw Error( "Format: " + format + " not allowed" );
        }    
    }

    /**
     * Formats MarcX records, either producing a Dublin Core record
     * or MarcX record(s)
     *
     * @function
     * @syntax OaiFormatter.convertXmlRecordStringsToMarcObjects( records )
     * @type {function}
     * @param {String[]} records Array consisting of marcxchange record strings
     * @returns {Record[]} new array of the same records in the same order but as Record objects
     * @name OaiFormatter.convertXmlRecordStringsToMarcObjects
     */
    function convertXmlRecordStringsToMarcObjects( records ) {

        Log.trace( "Entering OaiFormatter.convertXmlRecordStringsToMarcObjects" );

        var recordObjects = [ ];

        for ( var i = 0; i < records.length; i++ ) {
            var recordObject = MarcXchange.marcXchangeToMarcRecord( records[ i ] );
            recordObjects.push( recordObject );
        }

        Log.trace( "Leaving OaiFormatter.convertXmlRecordStringsToMarcObjects" );

        return recordObjects;

    }


    /**
     * Used for validating format
     * 
     * @returns {Array} list of allowed formats
     */
    function getAllowedFormats( ) {
        return [ 'oai_dc', 'marcx' ];
    }


    Log.info( "Leaving OaiFormatter module" );

    return {
        formatRecords: formatRecords,
        convertXmlRecordStringsToMarcObjects: convertXmlRecordStringsToMarcObjects,
        getAllowedFormats: getAllowedFormats
    };
}();
