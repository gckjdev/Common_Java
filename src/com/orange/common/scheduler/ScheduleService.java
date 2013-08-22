package com.orange.common.scheduler;

//import org.quartz.Scheduler;
//import org.quartz.SchedulerException;
//import org.quartz.impl.StdSchedulerFactory;
//import static org.quartz.JobBuilder.*;
//import static org.quartz.TriggerBuilder.*;
//import static org.quartz.SimpleScheduleBuilder.*;

import com.orange.common.utils.DateUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-20
 * Time: 下午9:53
 * To change this template use File | Settings | File Templates.
 */
public class ScheduleService {
    private static ScheduleService ourInstance = new ScheduleService();

    static Logger log = Logger.getLogger(ScheduleService.class.getName());

    public static ScheduleService getInstance() {
        return ourInstance;
    }

    private ScheduleService() {

        // Grab the Scheduler instance from the Factory
//        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
//
//        // and start it off
//        scheduler.start();

    }

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public void scheduleAtDate(Date date, Runnable runnable){
        log.info("<scheduleAtDate> task scheduled at date "+date.toString());
        scheduler.schedule(runnable, date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public void scheduleEveryday(int hour, int minute, int second, Runnable runnable){
        Date todayDate = DateUtil.getDateOfToday(hour, minute, second);
        log.info(String.format("<scheduleEveryday> task schedule at %02d:%02d:%02d, start from %s", hour, minute, second, todayDate.toString()));
        scheduler.scheduleAtFixedRate(runnable, todayDate.getTime() - System.currentTimeMillis(), 24*3600*1000, TimeUnit.MILLISECONDS);
    }

    public void scheduleEverySecond(int second, Runnable runnable){
        log.info(String.format("<scheduleEverySecond> task schedule every %d seconds", second));
        scheduler.scheduleAtFixedRate(runnable, 1, second, TimeUnit.SECONDS);
    }


}
