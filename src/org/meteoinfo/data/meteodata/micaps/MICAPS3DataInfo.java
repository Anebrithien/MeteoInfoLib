/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.meteodata.micaps;

import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IStationDataInfo;
import org.meteoinfo.data.meteodata.StationInfoData;
import org.meteoinfo.data.meteodata.StationModelData;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.Extent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.meteodata.MeteoDataType;

/**
 *
 * @author yaqiang
 */
public class MICAPS3DataInfo extends DataInfo implements IStationDataInfo {

    // <editor-fold desc="Variables">
    private String _description;
    private List<String> _varList = new ArrayList<String>();
    private List<String> _fieldList = new ArrayList<String>();
    private List<List<String>> _dataList = new ArrayList<List<String>>();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MICAPS3DataInfo() {
        this.setMissingValue(9999.0);
        this.setDataType(MeteoDataType.MICAPS_3);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">    

    @Override
    public void readDataInfo(String fileName) {
        BufferedReader sr = null;
        try {

            this.setFileName(fileName);
            int i;
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "gbk"));
            String[] dataArray;
            List<String> dataList = new ArrayList<String>();

            //Read file head
            String aLine = sr.readLine().trim();
            _description = aLine;
            //Read all lines
            aLine = sr.readLine().trim();
            String bLine;
            while ((bLine = sr.readLine()) != null) {
                aLine = aLine + " " + bLine.trim();
            }
            sr.close();
            dataArray = aLine.split("\\s+");
            dataList.clear();
            for (i = 0; i < dataArray.length; i++) {
                if (!dataArray[i].isEmpty()) {
                    dataList.add(dataArray[i]);
                }
            }

            int year = Integer.parseInt(dataList.get(0));
            if (year < 100) {
                if (year < 50) {
                    year = 2000 + year;
                } else {
                    year = 1900 + year;
                }
            }
            Calendar cal = new GregorianCalendar(year, Integer.parseInt(dataList.get(1)) - 1, Integer.parseInt(dataList.get(2)),
                    Integer.parseInt(dataList.get(3)), 0, 0);
            Date time = cal.getTime();
            int level = Integer.parseInt(dataList.get(4));
            int contourNum = Integer.parseInt(dataList.get(5));
            List<Float> contours = new ArrayList<Float>();
            for (i = 0; i < contourNum; i++) {
                contours.add(Float.parseFloat(dataList.get(6 + i)));
            }
            int idx = 6 + contourNum + 2;
            int pNum = Integer.parseInt(dataList.get(idx));
            idx += pNum * 2 + 1;
            int varNum = Integer.parseInt(dataList.get(idx));
            idx += 1;
            int stationNum = Integer.parseInt(dataList.get(idx));
            idx += 1;
            for (i = 0; i < varNum; i++) {
                _varList.add("Var" + String.valueOf(i + 1));
            }
            _fieldList.add("Stid");
            _fieldList.add("Longitude");
            _fieldList.add("Latitude");
            _fieldList.add("Altitude");
            _fieldList.addAll(_varList);
            while (idx + 3 + varNum < dataList.size()) {
                List<String> aData = new ArrayList<String>();
                for (int j = 0; j < 4 + varNum; j++) {
                    aData.add(dataList.get(idx));
                    idx += 1;
                }
                _dataList.add(aData);
            }

            stationNum = _dataList.size();
            Dimension tdim = new Dimension(DimensionType.T);
            double[] values = new double[1];
            values[0] = DataConvert.toOADate(time);
            tdim.setValues(values);
            this.setTimeDimension(tdim);
            Dimension zdim = new Dimension(DimensionType.Z);
            zdim.setValues(new double[]{level});
            this.setZDimension(zdim);
            List<Variable> variables = new ArrayList<Variable>();
            for (String vName : _varList) {
                Variable var = new Variable();
                var.setName(vName);
                var.setStation(true);
                var.setDimension(tdim);
                var.setDimension(zdim);
                variables.add(var);
            }
            this.setVariables(variables);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sr.close();
            } catch (IOException ex) {
                Logger.getLogger(MICAPS3DataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        dataInfo += System.getProperty("line.separator") + "Description: " + _description;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "Time: " + format.format(this.getTimes().get(0));
        dataInfo += System.getProperty("line.separator") + "Station Number: " + _dataList.size();
        dataInfo += System.getProperty("line.separator") + "Fields: ";
        for (String aField : _fieldList) {
            dataInfo += System.getProperty("line.separator") + "  " + aField;
        }

        return dataInfo;
    }

    @Override
    public StationData getStationData(int timeIdx, int varIdx, int levelIdx) {
        String stName;
        int i;
        double lon, lat;
        double t;
        t = 0;

        List<String> dataList;
        double[][] discreteData = new double[_dataList.size()][3];
        double minX, maxX, minY, maxY;
        minX = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        List<String> stations = new ArrayList<String>();

        //Get real variable index
        varIdx = _fieldList.indexOf(_varList.get(varIdx));

        for (i = 0; i < _dataList.size(); i++) {
            dataList = _dataList.get(i);
            stName = dataList.get(0);
            lon = Double.parseDouble(dataList.get(1));
            lat = Double.parseDouble(dataList.get(2));
            t = Double.parseDouble(dataList.get(varIdx));

            stations.add(stName);
            discreteData[i][0] = lon;
            discreteData[i][1] = lat;
            discreteData[i][2] = t;

            if (i == 0) {
                minX = lon;
                maxX = minX;
                minY = lat;
                maxY = minY;
            } else {
                if (minX > lon) {
                    minX = lon;
                } else if (maxX < lon) {
                    maxX = lon;
                }
                if (minY > lat) {
                    minY = lat;
                } else if (maxY < lat) {
                    maxY = lat;
                }
            }
        }
        Extent dataExtent = new Extent();
        dataExtent.minX = minX;
        dataExtent.maxX = maxX;
        dataExtent.minY = minY;
        dataExtent.maxY = maxY;

        StationData stData = new StationData();
        stData.data = discreteData;
        stData.dataExtent = dataExtent;
        stData.missingValue = this.getMissingValue();
        stData.stations = stations;

        return stData;
    }

    @Override
    public StationInfoData getStationInfoData(int timeIdx, int levelIdx) {
        StationInfoData stInfoData = new StationInfoData();
        stInfoData.setDataList(_dataList);
        stInfoData.setFields(_fieldList);
        stInfoData.setVariables(_varList);

        return stInfoData;
    }

    @Override
    public StationModelData getStationModelData(int timeIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}
