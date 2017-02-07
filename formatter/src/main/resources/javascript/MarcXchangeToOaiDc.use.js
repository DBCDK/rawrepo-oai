/** @file Module that creates simple Dublin Core elements based on marcxchange record. */

use( "Log" );
use( "Marc" );
use( "MarcXchange" );
use( "XmlNamespaces" );
use( "XmlUtil" );

EXPORTED_SYMBOLS = [ 'MarcXchangeToOaiDc' ];

/**
 * Module with functions that creates simple Dublin Core elements for OAI harvest of records from RawRepo.
 *
 * This module contains functions to create dc record
 *
 * @type {namespace}
 * @namespace
 */

var MarcXchangeToOaiDc = function() {

    Log.info( "Entering MarcXchangeToOaiDc module" );


    /**
     * Function that is entry to create a complete Dublin Core Record.
     *
     * @syntax MarcXchangeToOaiDc.createDcXml( marcXrecord )
     * @param {String} marcXrecord the marcxchange record to create DC from
     * @return {Document} the created DC record
     * @type {function}
     * @function
     * @name MarcXchangeToOaiDc.createDcXml
     */
    function createDcXml( marcXrecord ) {

        Log.trace( "Entering MarcXchangeToOaiDc.createDcXml" );

        var marcRecord = MarcXchange.marcXchangeToMarcRecord( marcXrecord );
        var oaiDcXml = XmlUtil.createDocument( "dc", XmlNamespaces.oai_dc );
        XmlUtil.addNamespace( oaiDcXml, XmlNamespaces.dc );
        XmlUtil.addNamespace( oaiDcXml, XmlNamespaces.xsi );
        XmlUtil.setAttribute( oaiDcXml, 
                              "schemaLocation", 
                              "http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd", 
                              XmlNamespaces.xsi );

        var map = new MatchMap( );
        MarcXchangeToOaiDc.addDcTitleElement( oaiDcXml, map );
        //call more functions
        marcRecord.eachFieldMap( map );

        Log.trace( "Leaving MarcXchangeToOaiDc.createDcXml" );

        return oaiDcXml;

    }

    /**
     * Function that puts eachField function on map to create dc:title from field 245 subfield a.
     *
     * @syntax MarcXchangeToOaiDc.addDcTitleElement( oaiDcXml, map )
     * @param {Document} oaiDcXml
     * @param {MatchMap} map
     * @type {function}
     * @function
     * @name MarcXchangeToOaiDc.addDcTitleElement
     */
    function addDcTitleElement( oaiDcXml, map ) {

        Log.trace( "Entering MarcXchangeToOaiDc.createDcTitle" );

        map.put( "245", function( field ) {
            var titles = [];
            field.eachSubField( "a", function( field, subfield ) {
                titles.push( subfield.value );
            } );
            var dcTitleValue = titles.join( ". " );
            XmlUtil.appendChildElement( oaiDcXml, "title", XmlNamespaces.dc, dcTitleValue );
        } );

        Log.trace( "Leaving MarcXchangeToOaiDc.createDcTitle" );

    }


    /**
     * Create a MatchMap, add mapping functions from a specific element method and build the dcxml from a record.
     * To be used when unit testing individual functions that adds elements to xml.
     *
     *
     * @type {function}
     * @syntax MarcXchangeToOaiDc.__callElementMethod( func, xml, record )
     * @param {Function} func the add-element function to call
     * @param {Document} xml the xml to add element to
     * @param {Record} record The record from which to create the element
     * @return {Document} xml with added element
     * @name MarcXchangeToOaiDc.__callElementMethod
     * @function
     */
    function __callElementMethod( func, xml, record ) {

        var map = new MatchMap();
        func( xml, map );
        record.eachFieldMap( map );

        return xml;
    }


    Log.info( "Leaving MarcXchangeToOaiDc module" );

    return {
        createDcXml: createDcXml,
        addDcTitleElement: addDcTitleElement,
        __callElementMethod: __callElementMethod
    }

}();