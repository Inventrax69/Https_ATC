package com.inventrax.atc.common.Log;




 public class Logger {

    public static void Log(String message) {

    }

    public static void Log(String message, Exception exception) {

       try {

        /*  ACRA.getErrorReporter().putCustomData(AppController.getUser().getLoginUserId() + "["+AppController.getUser().getUserId()+"]" , ( exception==null? " " : exception.getMessage() ) );

          File file = FileUtils.getLogFile("Log");

          FileWriter writer = new FileWriter(file);

          writer.append(message + " : \n " + exception.getMessage() );

          writer.flush();

          writer.close();*/

       }catch (Exception ex){

       }

    }

    public static void Log(Exception exception) {

    }


}
