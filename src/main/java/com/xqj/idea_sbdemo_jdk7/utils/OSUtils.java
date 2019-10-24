package com.xqj.idea_sbdemo_jdk7.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

@Slf4j
public class OSUtils {
    /**
     * 功能：获取Linux系统cpu使用率
     */
    public static Map<String, String> cpuUsage() {
        Map<String, String> cpuResult = new HashMap<>();
        try {
            Map<?, ?> map1 = OSUtils.cpuInfo();
            Thread.sleep(1 * 1000);
            Map<?, ?> map2 = OSUtils.cpuInfo();

            long user1 = Long.parseLong(map1.get("user").toString());
            long nice1 = Long.parseLong(map1.get("nice").toString());
            long system1 = Long.parseLong(map1.get("system").toString());
            long idle1 = Long.parseLong(map1.get("idle").toString());

            long user2 = Long.parseLong(map2.get("user").toString());
            long nice2 = Long.parseLong(map2.get("nice").toString());
            long system2 = Long.parseLong(map2.get("system").toString());
            long idle2 = Long.parseLong(map2.get("idle").toString());

            long total1 = user1 + system1 + nice1;
            long total2 = user2 + system2 + nice2;
            float total = total2 - total1;

            long totalIdle1 = user1 + nice1 + system1 + idle1;
            long totalIdle2 = user2 + nice2 + system2 + idle2;
            float totalidle = totalIdle2 - totalIdle1;

            float cpusage = (total / totalidle) * 100;
            cpuResult.put("result", "cpu使用率:" + String.format("%.2f", cpusage) + "%");
            return cpuResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
            cpuResult.put("result", e.toString());
        }
        return cpuResult;
    }


    /**
     * 功能：获取Linux系统内存使用率
     */
    public static Map<String, String> memoryUsage() {
        Map<String, String> memResult = new HashMap<>();
        Map<String, Object> map = new HashMap<String, Object>();
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null)
                    break;
                int beginIndex = 0;
                int endIndex = line.indexOf(":");
                if (endIndex != -1) {
                    String key = line.substring(beginIndex, endIndex);
                    beginIndex = endIndex + 1;
                    endIndex = line.length();
                    String memory = line.substring(beginIndex, endIndex);
                    String value = memory.replace("kB", "").trim();
                    map.put(key, value);
                }
            }

            long memTotal = Long.parseLong(map.get("MemTotal").toString()) / 1024;
            memResult.put("total", "内存总量:" + memTotal + "MB;");
            long memFree = Long.parseLong(map.get("MemFree").toString()) / 1024;
            memResult.put("free", "剩余内存:" + memFree + "MB;");
            long memused = memTotal - memFree;
            memResult.put("used", "已用内存:" + memused + "MB;");
            long buffers = Long.parseLong(map.get("Buffers").toString());
            long cached = Long.parseLong(map.get("Cached").toString());

            //原来是这个方法，我把它改一下
            //double usage = (double) (memused - buffers - cached) / memTotal * 100;
            double usage = (double) memused / memTotal * 100;
            memResult.put("result", memResult.get("total") + memResult.get("free") + memResult.get("used") + "内存使用率:" + String.format("%.2f", usage) + "%");
            buffer.close();
            inputs.close();
            return memResult;
        } catch (Exception e) {
            e.printStackTrace();
            memResult.put("total", "");
            memResult.put("free", "");
            memResult.put("used", "");
            memResult.put("result", e.toString());
        }
        return memResult;
    }

    /**
     * 功能：CPU使用信息
     */
    public static Map<?, ?> cpuInfo() {
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("cpu")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    List<String> temp = new ArrayList<String>();
                    while (tokenizer.hasMoreElements()) {
                        String value = tokenizer.nextToken();
                        temp.add(value);
                    }
                    map.put("user", temp.get(1));
                    map.put("nice", temp.get(2));
                    map.put("system", temp.get(3));
                    map.put("idle", temp.get(4));
                    map.put("iowait", temp.get(5));
                    map.put("irq", temp.get(6));
                    map.put("softirq", temp.get(7));
                    map.put("stealstolen", temp.get(8));
                    break;
                }
            }
            buffer.close();
            inputs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> networkUsage() {
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, String> map1 = networkInfo();
            Thread.sleep(1 * 1000);
            Map<String, String> map2 = networkInfo();
            if (!map1.get("name").equals("-1") && !map2.get("name").equals("-1")) {
                Long byteReceive1 = Long.parseLong(map1.get("byteReceive"));
                Long byteReceive2 = Long.parseLong(map2.get("byteReceive"));
                double byteReceive = byteReceive2 - byteReceive1;
                if (byteReceive / 1024 < 1024)
                    result.put("result", "当前服务器接收的网速是：" + String.format("%.2f", byteReceive / 1024) + "kb/s。");
                else
                    result.put("result", "当前服务器接收的网速是：" + String.format("%.2f", byteReceive / 1024 / 1024) + "Mb/s。");
                Long byteSend1 = Long.parseLong(map1.get("byteSend"));
                Long byteSend2 = Long.parseLong(map2.get("byteSend"));
                double byteSend = byteSend2 - byteSend1;
                if (byteSend / 1024 < 1024)
                    result.put("result", "当前服务器发送的网速是：" + String.format("%.2f", byteSend / 1024) + "kb/s" + ";  " + result.get("result"));
                else
                    result.put("result", "当前服务器发送的网速是：" + String.format("%.2f", byteSend / 1024 / 1024) + "Mb/s" + ";  " + result.get("result"));
                return result;
            } else
                result.put("result", "error");
        } catch (Exception e) {
            result.put("result", "当前服务器的收发网速是：" + e.toString());
        }
        return result;
    }

    /**
     * 功能：网络速度信息
     */
    public static Map<String, String> networkInfo() {
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/net/dev"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("  eth")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    while (tokenizer.hasMoreElements()) {
                        String value = tokenizer.nextToken();
                        if (value.contains(":")) {
                            map.put("name", value.split(":")[0]);
                            map.put("byteReceive", tokenizer.nextToken());
                            int i = 0;
                            while (i < 8) {
                                i++;
                                value = tokenizer.nextToken();
                            }
                            map.put("byteSend", value);
                            System.out.println(map.get("name") + "服务器接收的字节数是" + map.get("byteReceive"));
                            System.out.println(map.get("name") + "服务器发送的字节数是" + map.get("byteSend"));
                            buffer.close();
                            inputs.close();
                            return map;
                        }
                    }
                    break;
                }
            }
            buffer.close();
            inputs.close();
        } catch (Exception e) {
            e.printStackTrace();
            map.put("name", "-1");//-1表示出错了
            map.put("byteReceive", "-1");
            map.put("byteSend", "-1");
        }
        return map;
    }

    private static NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
    private static NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));
    private static DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");

    public static List<String> jvmInfo() {
        List<String> result = new LinkedList<>();
        //运行时情况
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        //操作系统情况
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        //线程使用情况
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        //堆内存使用情况
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        //非堆内存使用情况
        MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        //类加载情况
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        //内存池对象
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        //编译器和编译情况
        CompilationMXBean cm = ManagementFactory.getCompilationMXBean();
        //获取GC对象（不好用）
        List<GarbageCollectorMXBean> gcmList = ManagementFactory.getGarbageCollectorMXBeans();


        //运行时情况
        System.out.printf("jvm.name (JVM名称-版本号-供应商):%s | version: %s | vendor: %s  %n", runtime.getVmName(), runtime.getVmVersion(), runtime.getVmVendor());
        System.out.printf("jvm.spec.name (JVM规范名称-版本号-供应商):%s | version: %s | vendor: %s  %n", runtime.getSpecName(), runtime.getSpecVersion(), runtime.getSpecVendor());
        System.out.printf("jvm.java.version (JVM JAVA版本):%s%n", System.getProperty("java.version"));
        Date jvmStartTime = new Date(runtime.getStartTime());
        System.out.println("jvm.start.time (Java虚拟机的启动时间):" + jvmStartTime);
        System.out.printf("jvm.uptime (Java虚拟机的正常运行时间):%s%n", toDuration(runtime.getUptime()));

        System.out.println("------------------------------------------------------------------------------------------------------");

        result.add("JVM名称|版本号|供应商:" + runtime.getVmName() + " | " + runtime.getVmVersion() + " | " + runtime.getVmVendor() + ";");
        result.add("JVM规范名称|版本号|供应商:" + runtime.getSpecName() + " | " + runtime.getSpecVersion() + " | " + runtime.getSpecVendor() + ";");
        //result.add("jvm.start.time (Java虚拟机的启动时间):" + jvmStartTime);
        //result.add("jvm.uptime (Java虚拟机的正常运行时间):" + toDuration(runtime.getUptime()) + ";");
        //编译情况
        System.out.printf("compilation.name(编译器名称)：%s%n", cm.getName());
        System.out.printf("compilation.total.time(编译器耗时)：%d毫秒%n", cm.getTotalCompilationTime());
        boolean isSupport = cm.isCompilationTimeMonitoringSupported();
        if (isSupport) {
            System.out.println("支持即时编译器编译监控");
        } else {
            System.out.println("不支持即时编译器编译监控");
        }
        System.out.printf("------------------------------------------------------------------------------------------------------");
        //JVM 线程情况
        System.out.printf("jvm.threads.total.count (总线程数(守护+非守护)):%d%n", threads.getThreadCount());
        System.out.printf("jvm.threads.daemon.count (守护进程线程数):%d%n", threads.getDaemonThreadCount());
        System.out.printf("jvm.threads.peak.count (峰值线程数):%d%n", threads.getPeakThreadCount());
        System.out.printf("jvm.threads.total.start.count(Java虚拟机启动后创建并启动的线程总数):%d%n", threads.getTotalStartedThreadCount());
        for (Long threadId : threads.getAllThreadIds()) {
            System.out.printf("threadId: %d | threadName: %s%n", threadId, threads.getThreadInfo(threadId).getThreadName());
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
        //获取GC信息

        System.out.println("------------------------------------------------------------------------------------------------------");
        //堆内存情况
        System.out.printf("jvm.heap.init (初始化堆内存):%s %n", bytesToMB(heapMemoryUsage.getInit()));
        System.out.printf("jvm.heap.used (已使用堆内存):%s %n", bytesToMB(heapMemoryUsage.getUsed()));
        System.out.printf("jvm.heap.committed (可使用堆内存):%s %n", bytesToMB(heapMemoryUsage.getCommitted()));
        System.out.printf("jvm.heap.max (最大堆内存):%s %n", bytesToMB(heapMemoryUsage.getMax()));
        result.add("已使用堆内存:" + bytesToMB(heapMemoryUsage.getUsed()));
        result.add("可使用堆内存:" + bytesToMB(heapMemoryUsage.getCommitted()));
        System.out.println("------------------------------------------------------------------------------------------------------");

        //非堆内存使用情况
        System.out.printf("jvm.noheap.init (初始化非堆内存):%s %n", bytesToMB(nonHeapMemoryUsage.getInit()));
        System.out.printf("jvm.noheap.used (已使用非堆内存):%s %n", bytesToMB(nonHeapMemoryUsage.getUsed()));
        System.out.printf("jvm.noheap.committed (可使用非堆内存):%s %n", bytesToMB(nonHeapMemoryUsage.getCommitted()));
        System.out.printf("jvm.noheap.max (最大非堆内存):%s %n", bytesToMB(nonHeapMemoryUsage.getMax()));
        result.add("已使用非堆内存:" + bytesToMB(nonHeapMemoryUsage.getUsed()));
        result.add("可使用非堆内存:" + bytesToMB(nonHeapMemoryUsage.getCommitted()));
        System.out.println("------------------------------------------------------------------------------------------------------");

        //系统概况
        System.out.printf("os.name(操作系统名称-版本号):%s %s %s %n", os.getName(), "version", os.getVersion());
        System.out.printf("os.arch(操作系统内核):%s%n", os.getArch());
        System.out.printf("os.cores(可用的处理器数量):%s %n", os.getAvailableProcessors());
        System.out.printf("os.loadAverage(系统负载平均值):%s %n", os.getSystemLoadAverage());
        result.add("操作系统名称|版本号:" + os.getName() + " | " + os.getVersion());
        result.add("操作系统内核:" + os.getArch());
        result.add("可用的处理器数量:" + os.getAvailableProcessors());
        result.add("系统负载平均值:" + os.getSystemLoadAverage());
        System.out.println("------------------------------------------------------------------------------------------------------");

        //类加载情况
        System.out.printf("class.current.load.count(当前加载类数量):%s %n", cl.getLoadedClassCount());
        System.out.printf("class.unload.count(未加载类数量):%s %n", cl.getUnloadedClassCount());
        System.out.printf("class.total.load.count(总加载类数量):%s %n", cl.getTotalLoadedClassCount());

        System.out.println("------------------------------------------------------------------------------------------------------");

        for (MemoryPoolMXBean pool : pools) {
            final String kind = pool.getType().name();
            final MemoryUsage usage = pool.getUsage();
            System.out.println("内存模型： " + getKindName(kind) + ", 内存空间名称： " + getPoolName(pool.getName()) + ", jvm." + pool.getName() + ".init(初始化):" + bytesToMB(usage.getInit()));
            System.out.println("内存模型： " + getKindName(kind) + ", 内存空间名称： " + getPoolName(pool.getName()) + ", jvm." + pool.getName() + ".used(已使用): " + bytesToMB(usage.getUsed()));
            System.out.println("内存模型： " + getKindName(kind) + ", 内存空间名称： " + getPoolName(pool.getName()) + ", jvm." + pool.getName() + ".committed(可使用):" + bytesToMB(usage.getCommitted()));
            System.out.println("内存模型： " + getKindName(kind) + ", 内存空间名称： " + getPoolName(pool.getName()) + ", jvm." + pool.getName() + ".max(最大):" + bytesToMB(usage.getMax()));
            System.out.println("------------------------------------------------------------------------------------------------------");
        }
        return result;
    }

    /*public static List<Map<String, String>> diskInfo(){
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        File[] roots = File.listRoots();// 获取磁盘分区列表
        for (File file : roots) {
            Map<String, String> map = new HashMap<String, String>();

            long freeSpace=file.getFreeSpace();
            long totalSpace=file.getTotalSpace();
            long usableSpace=totalSpace-freeSpace;

            map.put("path", file.getPath());
            map.put("freeSpace", freeSpace / 1024 / 1024 / 1024 + "G");// 空闲空间
            map.put("usableSpace", usableSpace / 1024 / 1024 / 1024 + "G");// 可用空间
            map.put("totalSpace",totalSpace / 1024 / 1024 / 1024 + "G");// 总空间
            map.put("percent", DECIMALFORMAT.format(((double)usableSpace/(double)totalSpace)*100)+"%");// 总空间

            list.add(map);
        }

        return list;}*/

    public static List<Map<String, String>> diskInfo() {
        List<Map<String, String>> result = new LinkedList<>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("df -hl");// df -hl 查看硬盘空间
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String str = null;
                String[] line;
                int i = 0;
                while ((str = in.readLine()) != null) {
                    Map<String, String> lineMap = new LinkedHashMap<>();//expalin 这个不能放在while的外面，放外面的话，使用result.add,加进去的都是同一个对象，所以map进行clear的话，所有对象都会clear，所以都会变成和最后一个一样
                    line = str.split(" ");
                    if (i == 0) {
                        i++;
                        lineMap.clear();
                        lineMap.put("FileSystem", "文件系统");
                        lineMap.put("Size", "大小");
                        lineMap.put("Used", "已用");
                        lineMap.put("Avail", "空闲");
                        lineMap.put("Use%", "已用%");
                        lineMap.put("Mounton", "挂载路径");
                        result.add(lineMap);
                        continue;
                    }
                    int m = 0;
                    lineMap.clear();
                    for (String token : line) {
                        if (token.trim().length() == 0)
                            continue;
                        else {
                            if (m == 0)
                                lineMap.put("FileSystem", token);
                            if (m == 1)
                                lineMap.put("Size", token);
                            if (m == 2)
                                lineMap.put("Used", token);
                            if (m == 3)
                                lineMap.put("Avail", token);
                            if (m == 4)
                                lineMap.put("Use%", token);
                            if (m == 5)
                                lineMap.put("Mounton", token);
                            m++;
                        }
                    }
                    result.add(lineMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //未用过
    public static Desk diskInfo2() {
        Desk desk = new Desk();
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("df -hl");// df -hl 查看硬盘空间
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String str = null;
                String[] strArray = null;
                int line = 0;
                while ((str = in.readLine()) != null) {
                    line++;
                    if (line != 2) {
                        continue;
                    }
                    int m = 0;
                    strArray = str.split(" ");
                    for (String para : strArray) {
                        if (para.trim().length() == 0)
                            continue;
                        ++m;
                        if (para.endsWith("G") || para.endsWith("Gi")) {
                            // 目前的服务器
                            if (m == 2) {
                                desk.setTotal(para);
                            }
                            if (m == 3) {
                                desk.setUsed(para);
                            }
                        }
                        if (para.endsWith("%")) {
                            if (m == 5) {
                                desk.setUse_rate(para);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return desk;
    }

    //这个不太准，放弃使用
    public static List<Map<String, String>> diskIOInfo() {
        List<Map<String, String>> result = new LinkedList<>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("iostat -x -d -k");// df -hl 查看硬盘空间
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String str = null;
                String[] line;
                int i = 0;
                while ((str = in.readLine()) != null) {
                    Map<String, String> lineMap = new LinkedHashMap<>();//expalin 这个不能放在while的外面，放外面的话，使用result.add,加进去的都是同一个对象，所以map进行clear的话，所有对象都会clear，所以都会变成和最后一个一样
                    line = str.split(" ");
                    if (i == 1) {
                        int m = 0;
                        lineMap.clear();
                        String tempUtil = "";
                        for (String token : line) {
                            if (token.trim().length() == 0)
                                continue;
                            else {
                                if (m == 0)
                                    lineMap.put("Device", token);
                                if (m == 5)
                                    lineMap.put("rkB/s", token);
                                if (m == 6)
                                    lineMap.put("wkB/s", token);
                                m++;
                                tempUtil = token;
                            }
                        }
                        lineMap.put("%util", tempUtil);//因为这里不知道util的位置，不同的linux版本位置不一样
                        result.add(lineMap);
                    } else if (str.startsWith("Device")) {
                        i = 1;
                        lineMap.clear();
                        lineMap.put("Device", "设备");
                        lineMap.put("wkB/s", "硬盘读取(kB/s)");
                        lineMap.put("rkB/s", "硬盘写入(kB/s)");
                        lineMap.put("%util", "用于IO的时间占比");
                        result.add(lineMap);
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //用这个，上面那个不太准
    public static List<Map<String, String>> diskIOInfo2() {
        List<Map<String, String>> result = new LinkedList<>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("iotop -b -o -n 2");//第一个样本不准，还是取第二个好了
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String str = null;
                String[] line;
                int i = 0;
                int whichTotal = 0;
                while ((str = in.readLine()) != null) {
                    if (str.startsWith("Total")) {
                        whichTotal++;
                    }
                    if (whichTotal == 2) {//从第二个Total开始
                        Map<String, String> lineMap = new LinkedHashMap<>();//expalin 这个不能放在while的外面，放外面的话，使用result.add,加进去的都是同一个对象，所以map进行clear的话，所有对象都会clear，所以都会变成和最后一个一样
                        line = str.split(" ");
                        if (i == 2) {
                            int m = 0;
                            lineMap.clear();
                            for (String token : line) {
                                if (token.trim().length() == 0)
                                    continue;
                                else {
                                    if (m == 2)
                                        lineMap.put("User", token);
                                    if (m == 3)
                                        lineMap.put("DiskRead", token);
                                    if (m == 4)
                                        lineMap.put("DiskRead", lineMap.get("DiskRead") + token);
                                    if (m == 5)
                                        lineMap.put("DiskWrite", token);
                                    if (m == 6)
                                        lineMap.put("DiskWrite", lineMap.get("DiskWrite") + token);
                                    if (m == 10)
                                        lineMap.put("Command", "");
                                    if (m > 10)
                                        lineMap.put("Command", lineMap.get("Command") + " " + token);
                                    m++;
                                }
                            }
                            //System.out.println("~~~~~~~~~~~~~~~~~~"+lineMap.get("User"));
                            result.add(lineMap);
                        } else if (i == 1) {
                            i = 2;
                        } else if (i == 0) {
                            i = 1;
                            lineMap.clear();
                            lineMap.put("User", "用户");
                            lineMap.put("DiskRead", "硬盘读取");
                            lineMap.put("DiskWrite", "硬盘写入");
                            lineMap.put("Command", "命令");
                            result.add(lineMap);
                            //System.out.println("----------------"+lineMap.get("User"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 主入口
     *
     * @param args
     */
    public static void main(String[] args) {
        //1. 创建计时器类
        Timer timer = new Timer();
        //2. 创建任务类
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //cup使用率
                //float cpuUsage = cpuUsage();
                Map<String, String> cpuResult = cpuUsage();
                System.out.println(cpuResult.get("result"));
//        if(cpuUsage > 10.0 ){
//            SendMail.sendMail("xxxxx@qq.com", "服务器cpu使用率过高，请注意查看", "服务器提醒");
//        }
                //内存使用情况
                Map<String, String> memResult = cpuUsage();
                System.out.println(memResult.get("result"));
//        if((memoryUsage/1024) < 100){
//            SendMail.sendMail("xxxxx@qq.com","服务器内存剩余空间不足，请注意查看", "服务器提醒");
//        }
            }
        };
        timer.schedule(task, 1000, 1000 * 60);

    }

    //以下是借助方法

    protected static String getKindName(String kind) {
        if ("NON_HEAP".equals(kind)) {
            return "NON_HEAP(非堆内存)";
        } else {
            return "HEAP(堆内存)";
        }
    }

    protected static String getPoolName(String poolName) {
        switch (poolName) {
            case "Code Cache":
                return poolName + "(代码缓存区)";
            case "Metaspace":
                return poolName + "(元空间)";
            case "Compressed Class Space":
                return poolName + "(类指针压缩空间)";
            case "PS Eden Space":
                return poolName + "(伊甸园区)";
            case "PS Survivor Space":
                return poolName + "(幸存者区)";
            case "PS Old Gen":
                return poolName + "(老年代)";
            default:
                return poolName;
        }
    }


    protected static String bytesToMB(long bytes) {
        return fmtI.format((long) (bytes / 1024 / 1024)) + " MB";
    }

    protected static String printSizeInKb(double size) {
        return fmtI.format((long) (size / 1024)) + " kbytes";
    }

    protected static String toDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return fmtD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 24);
        String s = fmtI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
        }
        return s;
    }

    public static class Desk {
        private String total;
        private String used;
        private String use_rate;

        public String toString() {
            return "总磁盘空间：" + total + "，已使用：" + used + "，使用率达：" + use_rate;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getUsed() {
            return used;
        }

        public void setUsed(String used) {
            this.used = used;
        }

        public String getUse_rate() {
            return use_rate;
        }

        public void setUse_rate(String use_rate) {
            this.use_rate = use_rate;
        }

    }

}