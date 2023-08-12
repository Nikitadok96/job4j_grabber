package grabber;

import org.quartz.SchedulerException;

public interface Grab {
    void init() throws SchedulerException;
}
