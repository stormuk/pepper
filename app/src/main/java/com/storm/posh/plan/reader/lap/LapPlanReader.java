package com.storm.posh.plan.reader.lap;

import com.storm.posh.plan.Plan;
import com.storm.posh.plan.planelements.competence.Competence;
import com.storm.posh.plan.planelements.competence.CompetenceElement;
import com.storm.posh.plan.reader.PlanReader;
import com.storm.posh.plan.reader.lap.builders.CompetencesBuilder;
import com.storm.posh.plan.reader.lap.builders.DriveCollectionBuilder;
import com.storm.posh.plan.reader.lap.builders.ElementBuilder;
import com.storm.posh.plan.planelements.drives.DriveCollection;
import com.storm.posh.plan.planelements.drives.DriveElement;
import com.storm.posh.plan.reader.lap.builders.ActionPatternsBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public class LapPlanReader extends PlanReader {

    public LapPlanReader() {

    }

    @Override
    public void readFile(String fileName) {
        readLapFile(fileName);
    }

    private void readLapFile(String fileName) {
        //All of them are local parameter, we don't need them in the memory after their job is done.
        ActionPatternsBuilder actionPatternsBuilder = new ActionPatternsBuilder();
        CompetencesBuilder competencesBuilder = new CompetencesBuilder();
        DriveCollectionBuilder drivesBuilder = new DriveCollectionBuilder();
        ElementBuilder elementBuilder = new ElementBuilder();

        String line;

        // Read the file as one string.
        BufferedReader file;
        try {
            file = new BufferedReader(new FileReader(fileName));

            while ((line = file.readLine()) != null) {
                line = removeSpacesTabs(line);
                if (line.startsWith("(AP ")) {
                    Plan.getInstance().addActionPattern(actionPatternsBuilder.actionPatternElementBuilder(line));
                }
                if (line.startsWith("(C ")) {
                    Plan.getInstance().addCompetence(createNewCompetence(line, file, competencesBuilder, elementBuilder));
                }
                if (line.startsWith("(RDC ")) {
                    Plan.getInstance().addDriveCollection(createNewDriveCollection(line, file, drivesBuilder, elementBuilder));
                }
            }
        } catch (IOException e) {
            // TODO: Exception handling
            e.printStackTrace();
        }
    }

    private DriveCollection createNewDriveCollection(String currentLine, BufferedReader file, DriveCollectionBuilder drivesBuilder, ElementBuilder elementBuilder) throws IOException {
        String driveCollectionAsString = currentLine;

        List<DriveElement> elements = new LinkedList<>();

        boolean elementFound = true;
        while ((currentLine = file.readLine()) != null) {
            currentLine = removeSpacesTabs(currentLine);
            if (isStartOfElement(currentLine))
                elementFound = true;
            else if (isEndOfLine(currentLine))
                if (elementFound)
                    elementFound = false;
                else
                    break;
            else if (isDriveElement(currentLine)) {

                elements.add(elementBuilder.createDriveElement(currentLine));
            }
        }
        DriveCollection driveCollection = drivesBuilder.createDriveCollectionFromLAPFile(driveCollectionAsString);
        driveCollection.setDriveElements(elements);
        return driveCollection;
    }

    private Competence createNewCompetence(String currentLine, BufferedReader file, CompetencesBuilder competencesBuilder, ElementBuilder elementBuilder) throws IOException {
        String competenceAsString = currentLine;

        List<CompetenceElement> elements = new LinkedList<>();

        boolean elementFound = true;
        while ((currentLine = file.readLine()) != null) {
            // Remove spaces and tabs from the beginning of each line
            currentLine = removeSpacesTabs(currentLine);
            // If it is the start of an element declaration
            if (isStartOfElement(currentLine))
                elementFound = true;
                // If it is the end of an element declaration or competence
            else if (isEndOfLine(currentLine))
                // If it is the end of an element
                if (elementFound)
                    elementFound = false;
                    // It is the end of a declaration
                else
                    break;
            else if (isCompetenceElement(currentLine)) {
                elements.add(elementBuilder.createCompetenceElementFromLAPLine(currentLine));
            }
        }
        Competence competence = getCompetence(competenceAsString, competencesBuilder);
        competence.setCompetenceElements(elements);

        return competence;
    }

    private Competence getCompetence(String line, CompetencesBuilder competencesBuilder) {
        return competencesBuilder.createCompetenceFromLAPLine(line);
    }

    private String removeSpacesTabs(String line) {
        return line.trim();
    }

    private boolean isCompetenceElement(String line) {
        return !(line.toLowerCase().startsWith("(elements"));
    }

    private boolean isDriveElement(String line) {
        return !(line.toLowerCase().startsWith("(drives"));
    }

    private boolean isEndOfLine(String line) {
        return line.startsWith(")");
    }

    private boolean isStartOfElement(String line) {
        return line.length() == 1 && line.startsWith("(");
    }
}