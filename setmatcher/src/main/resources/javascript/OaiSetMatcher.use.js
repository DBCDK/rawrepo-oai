/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU GPL v3
 * See license text at https://opensource.dbc.dk/licenses/gpl-3.0
 */


/* global Log, MarcXchange */

/** @file Module with functions used to check which OAI sets a given MarcX record belongs to. */

use( "Log" );
use( "MarcXchange" );

EXPORTED_SYMBOLS = [ 'OaiSetMatcher' ];

/**
 * Module with functions used to check which OAI sets a given MarcX record belongs to.
 *
 * This module contains functions to validate
 *
 * @type {namespace}
 * @namespace
 */

var OaiSetMatcher = function() {

    Log.info( "Entering OaiSetMatcher module" );

    /**
     * Returns the names of OAI sets, in which the given MarcX record 
     * should be contained in.
     *
     * @syntax OaiSetMatcher.getOaiSets( agencyId, marcXrecord )
     * @param {Number} agencyId
     * @param {String} marcXrecord the marcxchange record
     * @return {Array} string array of OAI set names
     * @type {function}
     * @function
     * @name OaiSetMatcher.getOaiSets
     */
    function getOaiSets( agencyId, marcXrecord ) {

        Log.trace( "Entering SetMatcher.getOaiSets" );
                        
        var oaiSets = {};
        var marcRecord = MarcXchange.marcXchangeToMarcRecord( marcXrecord );

        var map = new MatchMap( );
        
        OaiSetMatcher.checkNAT( agencyId, oaiSets, map );
        OaiSetMatcher.checkBKM( agencyId, oaiSets, map );

        marcRecord.eachFieldMap( map );

        Log.trace( "Leaving SetMatcher.getOaiSets" );

        return __keys( oaiSets );

    }

    /**
     * Adds function to the MatchMap, which appends 'NAT' to oaiSets
     * if the given MarcX record should be contained in the NAT set.
     *
     * @syntax OaiSetMatcher.checkNAT( agencyId, oaiSets, map )
     * @param {Number} agencyId The agency that the record belongs to
     * @param {Object} oaiSets Object of oai set names
     * @param {MatchMap} map The map to register handler methods in
     * @type {function}
     * @function
     * @name OaiSetMatcher.checkNAT
     */
    function checkNAT( agencyId, oaiSets, map ) {

        Log.trace( "Entering OaiSetMatcher.checkNAT" );
        
        if ( 870970 !== agencyId && 870971 !== agencyId ) {
            Log.trace( "Leaving OaiSetMatcher.checkNAT" );
            return;
        }

        map.put( "032", function( field ) {
            if ( field.exists( "a" ) ) {
                oaiSets["NAT"] = true;
            }
        } );

        Log.trace( "Leaving OaiSetMatcher.checkNAT" );

    }
    
    /**
     * Adds function to the MatchMap, which appends 'BKM' to oaiSets
     * if the given MarcX record should be contained in the BKM set.
     *
     * @syntax OaiSetMatcher.checkBKM( agencyId, oaiSets, map )
     * @param {Number} agencyId The agency that the record belongs to
     * @param {Object} oaiSets Object of oai set names
     * @param {MatchMap} map The map to register handler methods in
     * @type {function}
     * @function
     * @name OaiSetMatcher.checkBKM
     */
    function checkBKM( agencyId, oaiSets, map ) {
        
        Log.trace( "Entering OaiSetMatcher.checkBKM" );
        
        if ( 870970 !== agencyId ) {
            Log.trace( "Leaving SetMatcher.checkBKM" );
            return;
        }
        
        map.put( "032", function( field ) {
            field.eachSubField( "x", function( field, subfield ) {
                var value = subfield.value.trim();
                if ( value.match( /^((BK[MRX])|(SF.)|(AC.)|(INV)|(UTI)|(NET))/i ) ) {
                    oaiSets["BKM"] = true;
                }            
            } );
        } );
        
        Log.trace( "Leaving OaiSetMatcher.checkBKM" );
    }
    
    /**
     * Returns keys of object as array of strings
     * 
     * @param {Object} obj
     * @returns {Array}
     */
    function __keys( obj ){
        var result = [ ];
        for ( var key in obj ) {
            if ( obj.hasOwnProperty( key ) ) {
                result.push( key );
            }
        }
        return result;
    }


    /**
     * Create a MatchMap, add mapping functions from a specific method and build OAI set array.
     * To be used when unit testing individual functions that do OAI set checking.
     *
     *
     * @type {function}
     * @syntax OaiSetMatcher.__callElementMethod( func, agencyId, record )
     * @param {Function} func the function to call
     * @param {Number} agencyId
     * @param {Record} record The record to be inspected 
     * @return {Array} array of OAI set names
     * @name OaiSetMatcher.__callElementMethod
     * @function
     */
    function __callElementMethod( func, agencyId, record ) {

        var oaiSets = {};
        var map = new MatchMap();
        func( agencyId, oaiSets, map );
        record.eachFieldMap( map );

        return __keys( oaiSets );
    }


    Log.info( "Leaving OaiSetMatcher module" );

    return {
        getOaiSets: getOaiSets,
        checkNAT: checkNAT,
        checkBKM: checkBKM,
        __callElementMethod: __callElementMethod
    };

}();
