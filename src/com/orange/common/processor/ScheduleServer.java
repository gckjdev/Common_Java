package com.orange.common.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;

import com.orange.common.log.ServerLog;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.StringUtil;

public class ScheduleServer implements Runnable {
    
	 public final static int UNDEFINE_HOUR = -1;
	
    private BlockingQueue<BasicProcessorRequest> queue = null;
    
    private MongoDBClient mongoClient = null;    
    
    private CommonProcessor processor;
    
    private static int requestCounter = 0;

    private static long startTime = 0;
    
    private int request_frequency = 20;    
    private int threadNum = 5;
    private int sleep_interval = 1000;
    private int resetHour = UNDEFINE_HOUR;
    
    public int getResetHour() {
		return resetHour;
	}

	private List<ScheduleServerProcessor> processorList;
    
    public ScheduleServer(CommonProcessor processor) {
        this.processor = processor;
        loadParam();
        createProcessThreads(processor);
    }
    
    public void loadParam() {
        if ( !StringUtil.isEmpty(System.getProperty("scheduleserver.request_frequency"))) {
            this.request_frequency = Integer.parseInt(System.getProperty("scheduleserver.frequency"));
           }
        if ( !StringUtil.isEmpty(System.getProperty("scheduleserver.threadNum"))) {
            this.threadNum = Integer.parseInt(System.getProperty("scheduleserver.threadnum"));
           }
        if ( !StringUtil.isEmpty(System.getProperty("scheduleserver.sleep_interval"))) {
            this.sleep_interval = Integer.parseInt(System.getProperty("scheduleserver.c"));
           }
    }

    public void createProcessThreads(CommonProcessor processor) {
        processorList = new ArrayList<ScheduleServerProcessor>();
        for (int i = 0; i < threadNum; i++) {
            
            ScheduleServerProcessor runnable = (ScheduleServerProcessor)processor;
            processorList.add(runnable);
            
            Thread thread = new Thread(runnable);
            thread.start();
            if (i == 0) {
                setQueue(runnable.getQueue());
                setMongoDBClient(runnable.getMongoDBClient());
               }
          }
        
       if (queue == null) {
            ServerLog.info(0, "<"+getClass().getSimpleName()+"> No queue available to use, bailing out.");
            return;
          }
    }    
    
    public void setMongoDBClient(MongoDBClient mongoDBClient) {
        this.mongoClient = mongoDBClient;
    }

    public void setQueue(BlockingQueue<BasicProcessorRequest> queue) {
        this.queue = queue;
    }

    
    private ScheduleServerProcessor getFirstProcessor(){
        return processorList.get(0);
    }

    public void run(){
        

        ScheduleServerProcessor processor = getFirstProcessor();
        
        ServerLog.info(0, "<"+getClass().getName()+">Reset all running message.");
        processor.resetAllRunningMessage();

        scheduleResetTimer();
        
        while (true) {
            try {
               if(!processor.canProcessRequest()) {
                   //sleep 1 minute
                   ServerLog.debug(0, "Sleeping, wake up util current time match process time");
                   Thread.sleep(60*1000);
                   continue;
                    }
                // get 1 record and put into queue
                BasicProcessorRequest request = processor.getProcessorRequest();
                // if there is no record, sleep one second
                if (request == null) {
                    ServerLog.debug(0, "<ScheduleServer> No request, sleep "+sleep_interval+" ms");
                    Thread.sleep(sleep_interval);
                } else {
                    queue.put(request);
                     }
                flowControl();
            } catch (Exception e) {
                ServerLog.error(0, e, "<ScheduleServer> catch Exception while running");
                 }
        }
    }

    private void scheduleResetTimer() {
        Timer resetTaskTimer = new Timer();
        Date fireDate = ResetTaskTimer.getTaskDate(resetHour);
        if (fireDate != null){
        	ServerLog.info(0, "<ScheduleServer>Schedule reset task timer at " + fireDate.toString());
        	resetTaskTimer.schedule(new ResetTaskTimer(this), fireDate);
           }
        else {
        	ServerLog.info(0, "no reset task timer set, reset hour = " + resetHour);
           }
	}

	private void flowControl() {
        try {
            requestCounter++;

            if (requestCounter == 1) {
                startTime = System.currentTimeMillis();
                }

            if (requestCounter == request_frequency) {
                long duration = System.currentTimeMillis() - startTime;
                if (duration < sleep_interval) {
                    long sleepTime = sleep_interval - duration;
                    ServerLog.info(0, "sleep " + sleepTime + " milliseconds for flow control");
                    Thread.sleep(sleepTime);
                     }
                requestCounter = 0;
                }
       } catch (InterruptedException e) {
            ServerLog.fatal(0, "<ScheduleServer> catch Exception while running.", e);
          }
    }

    public void setFrequency(int frequecy) {
        this.request_frequency = frequecy;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public void setInterval(int interval) {
        this.sleep_interval = interval;
    }

	public void setResetHour(int resetHour) {
		this.resetHour = resetHour;
	}

	// reset and activate all task in DB 
	static class ResetTaskTimer extends java.util.TimerTask{
		
		ScheduleServer scheduleServer;
		private static Logger log = Logger.getLogger(ResetTaskTimer.class.getName());
		
		public ResetTaskTimer(ScheduleServer server){
			this.scheduleServer = server;
		}
		
      @Override
      public void run() {
        	
        	// reset all running message
    		scheduleServer.getFirstProcessor().resetAllRunningMessage();
    		
    		// set next timer
    		Date fireDate = ResetTaskTimer.getTaskDate(scheduleServer.getResetHour());
    		if (fireDate != null){
    			Timer newResetTaskTimer = new Timer();
				newResetTaskTimer.schedule(new ResetTaskTimer(scheduleServer), fireDate);
    		}
        }
        
      public static Date getTaskDate(int scheduleHour){
        	
        	if (scheduleHour < 0 || scheduleHour >= 24){
        		return null;
        	}
        	
    		TimeZone timeZone = TimeZone.getTimeZone("GMT+0800");
    		Calendar now = Calendar.getInstance(timeZone);
    		now.setTime(new Date());
    		
    		if (now.get(Calendar.HOUR_OF_DAY) >= scheduleHour){
    			now.add(Calendar.DAY_OF_MONTH, 1);
    		}
    		
    		now.set(Calendar.HOUR_OF_DAY, scheduleHour);
    		now.set(Calendar.MINUTE, 0);
    		now.set(Calendar.SECOND, 0);
    		now.set(Calendar.MILLISECOND, 0);    			
    		
        	log.info("next reset task timer set to "+now.getTime().toString());    		
    		return now.getTime();
        }
    }
}
