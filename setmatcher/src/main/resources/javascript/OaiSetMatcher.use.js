
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
     * @param {String} agencyId
     * @param {String} marcXrecord the marcxchange record
     * @param {Object} recordFetcher Access object for fetching MarcX records from rawrepo
     * @return {Array} string array of OAI set names
     * @type {function}
     * @function
     * @name OaiSetMatcher.getOaiSets
     */
    function getOaiSets( agencyId, marcXrecord, recordFetcher ) {

        Log.trace( "Entering SetMatcher.getOaiSets" );
        
        // recordFetcher.fetchUnmerged( 870970, "bibRecId" );
        
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
     * @syntax OaiSetMatcher.checkNAT( oaiSets, map )
     * @param {String} agencyId
     * @param {Array} oaiSets Array of oai set names 
     * @param {MatchMap} map
     * @type {function}
     * @function
     * @name OaiSetMatcher.checkNAT
     */
    function checkNAT( agencyId, oaiSets, map ) {

        Log.trace( "Entering SetMatcher.checkNAT" );
        
        if( agencyId !== 870970 && agencyId !== 870971 ) {
            return;
        }

        map.put( "032", function( field ) {
            field.eachSubField( "a", function( field, subfield ) {
                var value = subfield.value.toUpperCase().trim();
                switch( value ) {
                    case 'DAN': 
                    case 'DAR': 
                    case 'DBF': 
                    case 'DBI': 
                    case 'DLF': 
                    case 'DMO': 
                    case 'GBF': 
                    case 'GMO': 
                    case 'DKF': 
                    case 'DMF': 
                    case 'DOP': 
                    case 'DPF': 
                    case 'DPO': 
                    case 'FPF': 
                    case 'GPF': 
                    case 'KIP': 
                    case 'FBL':
                        oaiSets["NAT"] = true;
                        break;
                    default:
                        break;
                }       
            } );
        } );

        Log.trace( "Leaving SetMatcher.checkNAT" );

    }
    
    /**
     * Adds function to the MatchMap, which appends 'BKM' to oaiSets
     * if the given MarcX record should be contained in the BKM set.
     *
     * @syntax OaiSetMatcher.checkBKM( oaiSets, map )
     * @param {String} agencyId
     * @param {Array} oaiSets Array of oai set names 
     * @param {MatchMap} map
     * @type {function}
     * @function
     * @name OaiSetMatcher.checkBKM
     */
    function checkBKM( agencyId, oaiSets, map ) {
        
        Log.trace( "Entering SetMatcher.checkBKM" );
        
        if( agencyId !== 870970 ) {
            return;
        }
        
        map.put( "032", function( field ) {
            field.eachSubField( "x", function( field, subfield ) {
                var value = subfield.value.toUpperCase().trim();
                if ( value.match(/^((BKM)|(SF.)|(AC.))$/) ) {
                    oaiSets["BKM"] = true;
                }            
            } );
        } );
        
        Log.trace( "Leaving SetMatcher.checkBKM" );
    }
    
    /**
     * Returns keys of object as array of strings
     * 
     * @param {Object} map
     * @returns {Array}
     */
    function __keys( map ){
        var result = [];
        for( var key in map ) {
            if( map.hasOwnProperty( key ) ) {
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
     * @syntax OaiSetMatcher.__callElementMethod( func, record )
     * @param {String} agencyId
     * @param {Function} func the function to call
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