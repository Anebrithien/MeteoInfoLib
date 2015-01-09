/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.table.DataTypes;

/**
 *
 * @author yaqiang
 */
public class StationTableData extends TableData{
    // <editor-fold desc="Variables">    
    private ProjectionInfo projInfo;
    private int lonIdx;
    private int latIdx;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public StationTableData(){
        this.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
        lonIdx = 0;
        latIdx = 1;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get projection info
     * @return Projection info
     */
    public ProjectionInfo getProjectionInfo(){
        return this.projInfo;
    }
    
    /**
     * Set projection info
     * @param value Projection info
     */
    public void setProjectionInfo(ProjectionInfo value){
        this.projInfo = value;
    }
    
    /**
     * Get longitude index
     * @return Longitude index
     */
    public int getLonIndex(){
        return this.lonIdx;
    }
    
    /**
     * Set longitude index
     * @param value Longitude index
     */
    public void setLonIndex(int value){
        this.latIdx = value;
    }
    
    /**
     * Get latitude index
     * @return Latitude index
     */
    public int getLatIndex(){
        return this.latIdx;
    }
    
    /**
     * Set Latitude index
     * @param value Latitude index
     */
    public void setLatIndex(int value){
        this.lonIdx = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Read data table from ASCII file
     *
     * @param fileName File name
     * @param lonIdx Longitude index
     * @param latIdx Latitude index
     * @throws java.io.FileNotFoundException
     */
    public void readASCIIFile(String fileName, int lonIdx, int latIdx) throws FileNotFoundException, IOException, Exception {
        this.lonIdx = lonIdx;
        this.latIdx = latIdx;
        DataTable dTable = new DataTable();

        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "utf-8"));
        String title = sr.readLine().trim();
        //Determine separator
        String separator = GlobalUtil.getSeparator(title);
        String[] titleArray = GlobalUtil.split(title, separator);
        if (titleArray.length < 2) {
            JOptionPane.showMessageDialog(null, "File Format Error!");
            sr.close();
        } else {
            //Get fields
            List<Integer> dataIdxs = new ArrayList<Integer>();
            String fieldName;
            for (int i = 0; i < titleArray.length; i++) {
                fieldName = titleArray[i];
                if (i == lonIdx || i == latIdx)
                    dTable.addColumn(fieldName, DataTypes.Float);
                else
                    dTable.addColumn(fieldName, DataTypes.String);
                dataIdxs.add(i);
            }

            String[] dataArray;
            int rn = 0;
            String line = sr.readLine();
            while (line != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                dataArray = GlobalUtil.split(line, separator);
                dTable.addRow();
                int cn = 0;
                for (int idx : dataIdxs) {
                    if (idx == lonIdx || idx == latIdx)
                        dTable.setValue(rn, cn, Float.parseFloat(dataArray[idx]));
                    else
                        dTable.setValue(rn, cn, dataArray[idx]);
                    cn++;
                }

                rn += 1;
                line = sr.readLine();
            }

            dataTable = dTable;
            sr.close();
        }
    }      
    
    /**
     * Clone
     * @return Cloned StationTableData object
     */
    @Override
    public Object clone(){
        StationTableData std = new StationTableData();
        std.dataTable = (DataTable)this.dataTable.clone();
        std.missingValue = this.missingValue;
        std.projInfo = this.projInfo;
        std.lonIdx = this.lonIdx;
        std.latIdx = this.latIdx;
        
        return std;
    }
    // </editor-fold>
}