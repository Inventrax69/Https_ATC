package com.inventrax.atc.pojos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Created by karthik.m on 06/12/2018.
 */


public class VehicleDTO {

    @SerializedName("VehicleID")
    private String vehicleID;
    @SerializedName("VehicleNumber ")
    private List<String> vehicleNumber ;
    @SerializedName("DocumentQuantity")
    private Double documentQuantity;
    @SerializedName("ReceivedQuantity")
    private Double receivedQuantity;
    @SerializedName("InboundList")
    private List<InboundDTO> inboundList;
    @SerializedName("ColorCodes ")
    private  List<ColorDTO> colorCodes ;
    @SerializedName("SLOC ")
    private   List<StorageLocationDTO> SLOC ;


    public VehicleDTO(Set<? extends Map.Entry<?, ?>> entries)
    {

        for(Map.Entry<?, ?> entry : entries)
        {

           switch(entry.getKey().toString())
           {

               case   "VehicleID":
                   this.setVehicleID(entry.getValue().toString());
                   break;
               case   "VehicleNumber":
                    this.setVehicleNumber((List<String>) entry.getValue());
                   break;
               case   "DocumentQuantity":
                   this.setDocumentQuantity(Double.parseDouble(entry.getValue().toString()));
                   break;
               case   "ReceivedQuantity":
                   this.setReceivedQuantity(Double.parseDouble(entry.getValue().toString()) );
                   break;
               case   "InboundList":
                   List<LinkedTreeMap<?,?>> treemapList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                   List<InboundDTO> lstInbound=new ArrayList<InboundDTO>();
                   for(int i=0;i<treemapList.size();i++)
                   {
                       InboundDTO dto=new InboundDTO(treemapList.get(i).entrySet());
                       lstInbound.add(dto);
                   }

                   this.setInboundList(lstInbound);
                   break;
               case "ColorCodes":
                   if(entry.getValue()!=null) {
                       List<LinkedTreeMap<?,?>> ColortreemapList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                       List<ColorDTO> lstColorCodes=new ArrayList<ColorDTO>();
                       for(int i=0;i<ColortreemapList.size();i++)
                       {
                           ColorDTO dto=new ColorDTO(ColortreemapList.get(i).entrySet());
                           lstColorCodes.add(dto);
                           //Log.d("Message", core.getEntityObject().toString());


                       }

                       this.setColorCodes(lstColorCodes);
                   }

                   break;
               case  "SLOC" :
                   if(entry.getValue()!=null) {
                       List<LinkedTreeMap<?,?>> SloctreemapList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                       List<StorageLocationDTO> lstSLOC=new ArrayList<StorageLocationDTO>();
                       for(int i=0;i<SloctreemapList.size();i++)
                       {
                           StorageLocationDTO dto=new StorageLocationDTO(SloctreemapList.get(i).entrySet());
                           lstSLOC.add(dto);
                           //Log.d("Message", core.getEntityObject().toString());


                       }

                       this.setSLOC(lstSLOC);
                   }
                   break;



           }

        }


    }




    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public List<String> getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(List<String> vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getDocumentQuantity() {
        return documentQuantity;
    }

    public void setDocumentQuantity(Double documentQuantity) {
        this.documentQuantity = documentQuantity;
    }

    public Double getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Double receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

   public List<InboundDTO> getInboundList() {
        return inboundList;
    }

    public void setInboundList(List<InboundDTO> inboundList) {
        this.inboundList = inboundList;
    }
    public List<ColorDTO> getColorCodes() {
        return colorCodes;
    }

    public void setColorCodes(List<ColorDTO> colorCodes) {
        this.colorCodes = colorCodes;
    }

    public List<StorageLocationDTO> getSLOC() {
        return SLOC;
    }

    public void setSLOC(List<StorageLocationDTO> SLOC) {
        this.SLOC = SLOC;
    }
}
