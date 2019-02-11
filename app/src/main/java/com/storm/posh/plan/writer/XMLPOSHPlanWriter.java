package com.storm.posh.plan.writer;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.ElementWithTrigger;
import com.storm.posh.plan.planelements.PlanElement;
import com.storm.posh.plan.planelements.Sense;
import com.storm.posh.plan.planelements.action.ActionEvent;
import com.storm.posh.plan.planelements.action.ActionPattern;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.planelements.drives.DriveElement;
import com.storm.posh.util.IWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * <p>
 *
 * @author :   Andreas Theodorou - www.recklesscoding.com
 * @version :   %G%
 */
public class XMLPOSHPlanWriter implements IWriter {

    @Override
    public void writeFile(String filePath) {
        savePlan(filePath);
    }

    private void savePlan(String filePath) {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Plan");
            doc.appendChild(rootElement);

            addAPElements(doc, rootElement);
            addCEElements(doc, rootElement);
            addCompElements(doc, rootElement);
            addDEsElements(doc, rootElement);
            addDElements(doc, rootElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void addAPElements(Document doc, Element rootElement) {
        Element subrootElement = createSubrootElement(doc, rootElement, "ActionPatterns");
        createAPElements(doc, subrootElement);
    }

    private void addCEElements(Document doc, Element rootElement) {
        Element subrootElement = createSubrootElement(doc, rootElement, "CompetenceElements");
        createCEElements(doc, subrootElement);
    }

    private void addCompElements(Document doc, Element rootElement) {
        Element subrootElement = createSubrootElement(doc, rootElement, "Competences");
        createCompElements(doc, subrootElement);
    }


    private void addDEsElements(Document doc, Element rootElement) {
        Element subrootElement = createDEsElement(doc, rootElement);
        createDEElements(doc, subrootElement);
    }


    private void addDElements(Document doc, Element rootElement) {
        Element subrootElement = createSubrootElement(doc, rootElement, "Drives");
        createDriveElements(doc, subrootElement);
    }

    private void createAPElements(Document doc, Element subrootElement) {
        for (ActionPattern actionPattern :
                Plan.getInstance().getActionPatterns()) {
            Element actionPatternElement = doc.createElement("ActionPattern");
            addNameAttr(doc, actionPattern, actionPatternElement);

            for (ActionEvent actionEvent :
                    actionPattern.getActionEvents()) {
                Element actionElement = doc.createElement("Action");
                addNameAttr(doc, actionEvent, actionElement);
                actionPatternElement.appendChild(actionElement);
            }
            subrootElement.appendChild(actionPatternElement);
        }
    }

    private void createCEElements(Document doc, Element subrootElement) {
        for (CompetenceElement competenceElement :
                Plan.getInstance().getCompetenceElements()) {
            Element competenceElementElement = doc.createElement("CompetenceElement");
            addNameAttr(doc, competenceElement, competenceElementElement);
            addTriggerAttr(doc, competenceElement, competenceElementElement);
            createSensesCE(doc, competenceElement, competenceElementElement);
            subrootElement.appendChild(competenceElementElement);
        }
    }

    private void createSensesCE(Document doc, CompetenceElement competenceElement, Element competenceElementElement) {
        Element sensesElement = createConditionsElement(doc, competenceElementElement);

        for (Sense sense :
                competenceElement.getSenses()) {
            Element senseElement = createSenseElement(doc, sense);
            sensesElement.appendChild(senseElement);
        }
    }

    private void createCompElements(Document doc, Element subrootElement) {
        for (Competence comptenece :
                Plan.getInstance().getCompetences()) {
            Element compteneceElement = doc.createElement("Competence");
            addNameAttr(doc, comptenece, compteneceElement);
            addGoalsToCompElement(doc, comptenece, compteneceElement);
            addCEToCompElement(doc, comptenece, compteneceElement);
            subrootElement.appendChild(compteneceElement);
        }
    }

    private void createDEElements(Document doc, Element subrootElement) {
        for (DriveElement driveElement :
                Plan.getInstance().getDriveElements()) {
            Element deElement = doc.createElement("DriveElement");
            addNameAttr(doc, driveElement, deElement);
            addTriggerAttr(doc, driveElement, deElement);
            addCheckTimeAttr(doc, driveElement, deElement);
            createSensesDE(doc, driveElement, deElement);
            subrootElement.appendChild(deElement);
        }
    }

    private void createSensesDE(Document doc, DriveElement driveElement, Element deElement) {
        Element sensesElement = createConditionsElement(doc, deElement);

        for (Sense sense :
                driveElement.getSenses()) {
            Element senseElement = createSenseElement(doc, sense);
            sensesElement.appendChild(senseElement);
        }
    }

    private void createDriveElements(Document doc, Element subrootElement) {
        for (DriveCollection driveCollection :
                Plan.getInstance().getDriveCollections()) {
            Element driveElement = doc.createElement("Drive");
            addNameAttr(doc, driveCollection, driveElement);
            addGoalsToDriveElement(doc, driveCollection, driveElement);
            addDEsToDriveElement(doc, driveCollection, driveElement);
            subrootElement.appendChild(driveElement);
        }
    }

    private void addGoalsToCompElement(Document doc, Competence competence, Element competenceElement) {
        Element sensesElement = createGoalsElement(doc, competenceElement);

        for (Sense sense :
                competence.getGoals()) {
            Element senseElement = createSenseElement(doc, sense);
            sensesElement.appendChild(senseElement);
        }
    }

    private void addGoalsToDriveElement(Document doc, DriveCollection driveCollection, Element driveElement) {
        Element sensesElement = createGoalsElement(doc, driveElement);

        for (Sense sense :
                driveCollection.getGoals()) {
            Element senseElement = createSenseElement(doc, sense);
            sensesElement.appendChild(senseElement);
        }
    }

    private void addDEsToDriveElement(Document doc, DriveCollection driveCollection, Element driveElement) {
        Element sensesElement = createDEsElement(doc, driveElement);

        for (DriveElement de :
                driveCollection.getDriveElements()) {
            Element deElement = createDEElement(doc, de);
            sensesElement.appendChild(deElement);
        }
    }

    private void addCEToCompElement(Document doc, Competence competence, Element competenceElement) {
        Element sensesElement = createCEsElementForCompElement(doc, competenceElement);

        for (CompetenceElement ce :
                competence.getCompetenceElements()) {
            Element ceElement = createCEElement(doc, ce);
            sensesElement.appendChild(ceElement);
        }
    }

    private Element createSubrootElement(Document doc, Element rootElement, String elementName) {
        Element actionPatternsElement = doc.createElement(elementName);
        rootElement.appendChild(actionPatternsElement);

        return actionPatternsElement;
    }

    private Element createCEsElementForCompElement(Document doc, Element competenceElement) {
        Element goalsElement = doc.createElement("CompetenceElements");
        competenceElement.appendChild(goalsElement);

        return goalsElement;
    }

    private Element createGoalsElement(Document doc, Element competenceElement) {
        Element goalsElement = doc.createElement("Goals");
        competenceElement.appendChild(goalsElement);

        return goalsElement;
    }

    private Element createConditionsElement(Document doc, Element element) {
        Element sensesElement = doc.createElement("Senses");
        element.appendChild(sensesElement);
        return sensesElement;
    }

    private Element createDEsElement(Document doc, Element element) {
        Element sensesElement = doc.createElement("DriveElements");
        element.appendChild(sensesElement);
        return sensesElement;
    }

    private Element createSenseElement(Document doc, Sense sense) {
        Element senseElement = doc.createElement("Sense");
        addNameAttr(doc, sense, senseElement);

        Attr attrValue = doc.createAttribute("value");
        attrValue.setValue(sense.getValue());
        senseElement.setAttributeNode(attrValue);

        Attr attrComparator = doc.createAttribute("comparator");
        attrComparator.setValue(sense.getComparator());
        senseElement.setAttributeNode(attrComparator);
        return senseElement;
    }

    private Element createCEElement(Document doc, CompetenceElement ce) {
        Element ceElement = doc.createElement("CompetenceElement");
        addNameAttr(doc, ce, ceElement);

        return ceElement;
    }

    private Element createDEElement(Document doc, DriveElement de) {
        Element deElement = doc.createElement("DriveElement");
        addNameAttr(doc, de, deElement);

        return deElement;
    }

    private void addNameAttr(Document doc, PlanElement planElement, Element xmlElement) {
        Attr attrName = doc.createAttribute("name");
        attrName.setValue(planElement.getNameOfElement());
        xmlElement.setAttributeNode(attrName);
    }

    private void addTriggerAttr(Document doc, ElementWithTrigger planElement, Element xmlElement) {
        Attr attrTrigger = doc.createAttribute("triggers");
        attrTrigger.setValue(planElement.getTriggeredElement().getNameOfElement());
        xmlElement.setAttributeNode(attrTrigger);
    }

    private void addCheckTimeAttr(Document doc, DriveElement driveElement, Element xmlElement) {
        Attr attrTrigger = doc.createAttribute("checkTime");
        attrTrigger.setValue(String.valueOf(driveElement.getFrequencyValue()));
        xmlElement.setAttributeNode(attrTrigger);
    }

}