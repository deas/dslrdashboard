package com.dslr.dashboard;

/**
 * @author Ashok Gelal
 * copyright: Ashok
 */

public class TimeSpan{
    public static final long TicksPerMillisecond = 10000L;
    public static final long TicksPerSecond = 10000000L;
    public static final long TicksPerMinute = 600000000L;
    public static final long TicksPerHour = 36000000000L;
    public static final long TicksPerDay = 864000000000L;
    public static final TimeSpan Zero = new TimeSpan(0);
    public static final TimeSpan MinValue = new TimeSpan(Long.MIN_VALUE);
    public static final TimeSpan MaxValue = new TimeSpan(Long.MAX_VALUE);

    private long ticks;

    public int Hours() {
        return hours;
    }

    public int Minutes() {
        return minutes;
    }

    public int Seconds() {
        return seconds;
    }

    public int Days() {
        return days;
    }

    public int Milliseconds() {
        return milliseconds;
    }

    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;

    private void TotalDays(double totalDays) {
        this.totalDays = totalDays;
    }

    private void TotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    private void TotalMinutes(double totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    private void TotalSeconds(double totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public double TotalDays() {
        return totalDays;
    }

    public double TotalHours() {
        return totalHours;
    }

    public double TotalMinutes() {
        return totalMinutes;
    }

    public double TotalSeconds() {
        return totalSeconds;
    }

    public double TotalMilliseconds() {
        return totalMilliseconds;
    }

    private void TotalMilliseconds(double totalMilliseconds) {
        this.totalMilliseconds = totalMilliseconds;
    }

    private double totalDays;
    private double totalHours;
    private double totalMinutes;
    private double totalSeconds;
    private double totalMilliseconds;

    public long Ticks() {
        return ticks;
    }

    public TimeSpan(long ticks) {
        this.ticks = ticks;
        ConvertTicksToTotalTime();
        ConvertTicksToTime();
    }

    private void ConvertTicksToTime() {
        days = (int)(ticks / (TicksPerDay+0.0));
        long diff = (ticks - TicksPerDay * days);
        hours = (int)(diff / (TicksPerHour+0.0));
        diff = (diff - TicksPerHour * hours);
        minutes = (int)(diff / (TicksPerMinute+0.0));
        diff = (diff - TicksPerMinute * minutes);
        seconds = (int)(diff / (TicksPerSecond + 0.0));
        diff = (diff - TicksPerSecond * seconds);
        milliseconds = (int)((diff / TicksPerMillisecond+0.0));
    }

    private void ConvertTicksToTotalTime() {
        TotalDays(ticks / (TicksPerDay + 0.0f));
        TotalHours(ticks / (TicksPerHour + 0.0f));
        TotalMinutes(ticks / (TicksPerMinute + 0.0f));
        TotalSeconds(ticks/(TicksPerSecond+0.0f));
        TotalMilliseconds(ticks/(TicksPerMillisecond+0.0f));
    }

    public TimeSpan(int hours, int minutes, int seconds) {
        this(0, hours, minutes, seconds);
    }

    public TimeSpan(int days, int hours, int minutes, int seconds) {
        this(days, hours, minutes, seconds, 0);
    }

    public TimeSpan(int days, int hours, int minutes, int seconds, int milliseconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        CalculateTicks();
    }

    private void CalculateTicks(){
        this.ticks = days * TicksPerDay + hours * TicksPerHour + minutes * TicksPerMinute + seconds * TicksPerSecond + milliseconds * TicksPerMillisecond;
        ConvertTicksToTotalTime();
    }
    
    public static TimeSpan Add(TimeSpan t1, TimeSpan t2)
    {
        return new TimeSpan(t1.ticks+t2.ticks);
    }

    public TimeSpan Add(TimeSpan t1)
    {
       return new TimeSpan(this.ticks + t1.ticks);
    }

    @Override
    public boolean equals(Object other){
        if(other == null) return false;
        if(other == this) return true;
        if(this.getClass() != other.getClass()) return false;
        TimeSpan otherClass = (TimeSpan) other;
        return (ticks==otherClass.Ticks());
    }

    public boolean Equals(TimeSpan other)
    {
        return equals(other);
    }

    public static boolean Equals(TimeSpan time1, TimeSpan time2)
    {
        return time1.Equals(time2);
    }

    public boolean GreaterThan(TimeSpan time)
    {
        return ticks > time.Ticks();
    }

    public boolean GreaterThanOrEqual(TimeSpan time)
    {
        return ticks >= time.Ticks();
    }

    public boolean NotEquals(TimeSpan time)
    {
        return !Equals(time);
    }

    public boolean LessThan(TimeSpan time)
    {
        return ticks < time.Ticks();
    }

    public boolean LessThanOrEqual(TimeSpan time)
    {
        return ticks <= time.Ticks();
    }

    public TimeSpan Subtract(TimeSpan time)
    {
        return new TimeSpan(ticks - time.Ticks());
    }

    public static TimeSpan Subtract(TimeSpan time1, TimeSpan time2)
    {
        return new TimeSpan(time1.Ticks() - time2.Ticks());
    }

    public TimeSpan Duration()
    {
        return new TimeSpan(Math.abs(ticks));
    }

    public static TimeSpan FromDays(double days)
    {
        return new TimeSpan((long)(Math.ceil(days * 24 * 3600 * 1000) * TicksPerMillisecond));
    }

    public static TimeSpan FromHours(double hours)
    {
        return new TimeSpan((long)(Math.ceil(hours * 3600 * 1000) * TicksPerMillisecond));
    }

    public static TimeSpan FromMinutes(double minutes)
    {
        return new TimeSpan((long)(Math.ceil(minutes * 60 * 1000) * TicksPerMillisecond));
    }

    public static TimeSpan FromSeconds(double seconds)
    {
        return new TimeSpan((long)(Math.ceil(seconds * 1000) * TicksPerMillisecond));
    }

    public static TimeSpan FromMilliseconds(double milliseconds)
    {
        return new TimeSpan((long)(Math.ceil(milliseconds) * TicksPerMillisecond));
    }

    public static TimeSpan FromTicks(long ticks)
    {
        return new TimeSpan(ticks);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(days>=1 || days<=-1)
            str.append(String.format("%02d.", days));

        str.append(String.format("%02d:", hours));
        str.append(String.format("%02d:", minutes));
        str.append(String.format("%02d", seconds));

        if(milliseconds>=1)
            str.append(String.format(".%d%s", milliseconds, TRAILING_ZEROS.substring(Integer.toString(milliseconds).length())));
        return str.toString();
    }

    private static final String TRAILING_ZEROS = "0000000";
}