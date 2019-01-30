package com.storm.posh.plan.reader;

import com.storm.posh.util.IReader;

/**
 * Author: @Andreas.
 * Date : @29/12/2015
 */
public abstract class PlanReader implements IReader {

    @Override
    public abstract void readFile(String fileName);

}
