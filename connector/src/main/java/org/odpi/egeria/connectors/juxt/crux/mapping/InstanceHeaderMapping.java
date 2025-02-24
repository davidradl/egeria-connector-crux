/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.juxt.crux.mapping;

import crux.api.CruxDocument;
import org.odpi.egeria.connectors.juxt.crux.auditlog.CruxOMRSAuditCode;
import org.odpi.egeria.connectors.juxt.crux.repositoryconnector.CruxOMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceHeader;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefCategory;

import java.util.HashSet;
import java.util.Set;

/**
 * Maps the properties of InstanceHeaders between persistence and objects.
 * (Note that this is the first level of mapping that can be instantiated, as it is the first level
 * in which a Crux ID (GUID) is mapped.)
 */
public class InstanceHeaderMapping extends InstanceAuditHeaderMapping {

    private static final String INSTANCE_HEADER = "InstanceHeader";

    private static final String INSTANCE_URL = "instanceURL";
    private static final String RE_IDENTIFIED_FROM_GUID = "reIdentifiedFromGUID";

    private static final Set<String> KNOWN_PROPERTIES = createKnownProperties();
    private static Set<String> createKnownProperties() {
        Set<String> set = new HashSet<>();
        set.add(INSTANCE_URL);
        set.add(RE_IDENTIFIED_FROM_GUID);
        return set;
    }

    protected InstanceHeader instanceHeader;
    protected CruxDocument cruxDoc;

    /**
     * Construct a mapping from an InstanceAuditHeader (to map to a Crux representation).
     * @param cruxConnector connectivity to Crux
     * @param instanceHeader from which to map
     */
    protected InstanceHeaderMapping(CruxOMRSRepositoryConnector cruxConnector,
                                    InstanceHeader instanceHeader) {
        super(cruxConnector);
        this.instanceHeader = instanceHeader;
    }

    /**
     * Construct a mapping from a Crux map (to map to an Egeria representation).
     * @param cruxConnector connectivity to Crux
     * @param cruxDoc from which to map
     */
    protected InstanceHeaderMapping(CruxOMRSRepositoryConnector cruxConnector,
                                    CruxDocument cruxDoc) {
        super(cruxConnector);
        this.cruxDoc = cruxDoc;
    }

    /**
     * Map from Egeria to Crux.
     * @return CruxDocument
     * @see #InstanceHeaderMapping(CruxOMRSRepositoryConnector, InstanceHeader)
     */
    public CruxDocument toCrux() {
        if (cruxDoc == null && instanceHeader != null) {
            cruxDoc = toDoc().build();
        }
        return cruxDoc;
    }

    /**
     * Translate the provided Egeria representation into a Crux document.
     * @return CruxDocument.Builder from which to build the document
     */
    protected CruxDocument.Builder toDoc() {
        CruxDocument.Builder builder = CruxDocument.builder(getGuidReference(cruxConnector, instanceHeader));
        super.buildDoc(builder, instanceHeader);
        builder.put(INSTANCE_URL, instanceHeader.getInstanceURL());
        builder.put(RE_IDENTIFIED_FROM_GUID, instanceHeader.getReIdentifiedFromGUID());
        return builder;
    }

    /**
     * Translate the provided Crux representation into an Egeria representation.
     */
    protected void fromDoc() {
        super.fromDoc(instanceHeader, cruxDoc);
        final String methodName = "fromDoc";
        String guid = (String) cruxDoc.getId();
        instanceHeader.setGUID(guid == null ? null : trimGuidFromReference(guid));
        for (String property : KNOWN_PROPERTIES) {
            Object objValue = cruxDoc.get(property);
            String value = objValue == null ? null : objValue.toString();
            if (INSTANCE_URL.equals(property)) {
                instanceHeader.setInstanceURL(value);
            } else if (RE_IDENTIFIED_FROM_GUID.equals(property)) {
                instanceHeader.setReIdentifiedFromGUID(value);
            } else {
                cruxConnector.logProblem(this.getClass().getName(),
                        methodName,
                        CruxOMRSAuditCode.UNMAPPED_PROPERTY,
                        null,
                        property,
                        INSTANCE_HEADER);
            }
        }
    }

    /**
     * Translate the provided InstanceHeader information into a Crux reference to the GUID of the instance.
     * @param cruxConnector connectivity to the repository
     * @param ih to translate
     * @return String for the Crux reference
     */
    public static String getGuidReference(CruxOMRSRepositoryConnector cruxConnector, InstanceHeader ih) {
        final String methodName = "getGuidReference";
        TypeDefCategory type = ih.getType().getTypeDefCategory();
        if (type.equals(TypeDefCategory.ENTITY_DEF)) {
            return getReference(EntitySummaryMapping.INSTANCE_REF_PREFIX, ih.getGUID());
        } else if (type.equals(TypeDefCategory.RELATIONSHIP_DEF)) {
            return getReference(RelationshipMapping.INSTANCE_REF_PREFIX, ih.getGUID());
        } else {
            cruxConnector.logProblem(InstanceHeaderMapping.class.getName(),
                    methodName,
                    CruxOMRSAuditCode.NON_INSTANCE_RETRIEVAL,
                    null,
                    type.name());
            return null;
        }
    }

    /**
     * Retrieve only the GUID portion of a Crux reference.
     * @param reference from which to trim the GUID
     * @return String of only the GUID portion of the reference
     */
    public static String trimGuidFromReference(String reference) {
        return reference.substring(reference.indexOf("_") + 1);
    }

    /**
     * Translate the provided details into a Crux reference.
     * @param instanceType of the instance (from TypeDefCategory name)
     * @param guid of the instance
     * @return String for the Crux reference
     */
    protected static String getReference(String instanceType, String guid) {
        return instanceType + "_" + guid;
    }

}
