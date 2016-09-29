/**
 * Created by Z on 2016/9/5.
 */
package com.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
//import java.util.Arrays;
import java.util.Iterator;


class Flow {
    String dataSet;//数据集名
    String srcIp;//源IP
    String dstIp;//目的IP
    double up_pl_bytes;//上行载荷总量
    double duration;//持续时间
    double up_avg_ipt;//上行平均时间间隔
    double dw_avg_ipt;//下行平均时间间隔
    double up_max_ipt;//上行最大时间间隔
    double dw_max_ipt;//下行最大时间间隔

    Flow() {
        this.dataSet = null;
        this.srcIp = null;
        this.dstIp = null;
        this.up_pl_bytes = -1;
        this.duration = -1;
        this.up_avg_ipt = -1;
        this.dw_avg_ipt = -1;
        this.up_max_ipt = -1;
        this.dw_max_ipt = -1;
    }

    void setDataSet(String s) {
        this.dataSet = s;
    }

    void setSrcIp(String s) {
        this.srcIp = s;
    }

    void setDstIp(String s) {
        this.dstIp = s;
    }

    void setUp_pl_bytes(double d) {
        this.up_pl_bytes = d;
    }

    void setDuration(double d) {
        this.duration = d;
    }

    void setUp_avg_ipt(double d) {
        this.up_avg_ipt = d;
    }

    void setDw_avg_ipt(double d) {
        this.dw_avg_ipt = d;
    }

    void setUp_max_ipt(double d) {
        this.up_max_ipt = d;
    }

    void setDw_max_ipt(double d) {
        this.dw_max_ipt = d;
    }
}

class Bundleip {
    String src_dst_ip = null;                   // 保存具有相同源IP和目的IP的IP字段
    String[] dataset = new String[36];          //保存数据集的名字
    int flowNumber = 0;                         //存在对应源IP和目的IP的流的数量
    boolean isTheSameDataset = true;            //存在对应源IP和目的IP是否在同一个数据集
    double flow_sum_up_pl_bytes = 0;            //IP对应的流的上行载荷总量，与flowNumber一起用于计算后面的平均值
    double flow_sum_duration = 0;               //IP对应的流的持续时间总量，与flowNumber一起用于计算后面的平均值
    boolean up_avg_iptExistSmall = false;       //上行平均时间间隔存在很小值
    boolean dw_avg_iptExistSmall = false;       //下行平均时间间隔存在很小值
    boolean up_avg_iptExistZero = false;        //上行平均时间间隔存在0
    boolean dw_avg_iptExistZero = false;        //下行平均时间间隔存在0
    boolean up_max_iptExistZero = false;
    boolean dw_max_iptExistZero = false;
    boolean up_avg_ipt_isSame = false;          //对应的流的上行平均时间间隔值一样
    double flow_sum_up_avg_ipt = 0;
    boolean dw_avg_ipt_isSame = false;
    double flow_sum_dw_avg_ipt = 0;
    boolean up_max_ipt_isSame = false;
    double flow_sum_up_max_ipt = 0;
    boolean dw_max_ipt_isSame = false;
    double flow_sum_dw_max_ipt = 0;
    double up_pl_bytesAverageNumb = 0.0;             //处于平均范围的数量
    double durationAverageNumb = 0.0;
    //boolean up_pl_bytes_isAverage = false;       //对应流的上行载荷总量是否平均
    //boolean duration_isAverage = false;
}

public class DatasetAnalysis {
    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";
    private static final int BINUMB = 100;
    private static final int FLOWSUMNUM = 1533;

    /**
     * 判断Excel的版本,获取Workbook
     *
     * @param in
     * @param filename
     * @return
     * @throws IOException
     */
    public static Workbook getWordkbook(InputStream in, File filename) throws IOException {
        Workbook wb = null;
        if (filename.getName().endsWith(EXCEL_XLS)) {
            wb = new HSSFWorkbook(in);
        } else if (filename.getName().endsWith(EXCEL_XLSX)) {
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    /**
     * 判断文件是否是excel
     *
     * @throws Exception
     */
    public static void checkExcelValid(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("no such file!");
        }
        if (!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))) {
            throw new Exception("not excel file!");
        }
    }

    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     *
     * @throws Exception
     */

    /**
     * 统计有多少对应的流
     */
    private static Bundleip[] countTheSameFlow(Flow[] flows) {
        int i, j, k;
        Bundleip[] bi = new Bundleip[BINUMB];
        for (j = 0; j < BINUMB; j++) {//先实例化所有的捆绑对象
            bi[j] = new Bundleip();
        }
        for (i = 0; i < FLOWSUMNUM; i++) {//统计源IP和目的IP对应的流的数量，总数居是1533条
            for (j = 0, k = 0; j < BINUMB; j++) {
                if (bi[j].src_dst_ip == null) {
                    bi[j].src_dst_ip = (flows[i].srcIp + '/' + flows[i].dstIp);//判断当前的对象的源IP+目的IP串是不是空的，是空的说明还没被赋值，将读取到的流的IP串赋值给它
                    bi[j].flowNumber++;                                         //同时存在源IP和目的IP对应的流的个数加1
                    bi[j].flow_sum_up_pl_bytes += flows[i].up_pl_bytes;         //计算同样一条对应流的上行载荷总量的总数，以便待会儿求平均
                    bi[j].flow_sum_duration += flows[i].duration;               //计算同样一条对应流的持续时间的总数，以便待会儿求平均
                    bi[j].flow_sum_up_avg_ipt += flows[i].up_avg_ipt;
                    bi[j].flow_sum_dw_avg_ipt += flows[i].dw_avg_ipt;
                    bi[j].flow_sum_up_max_ipt += flows[i].up_max_ipt;
                    bi[j].flow_sum_dw_max_ipt += flows[i].dw_max_ipt;
                    bi[j].dataset[k] = flows[i].dataSet;                        //将当前读取到流的数据集名称赋值到对象中保存数据集名称的变量中去

                    //上行数据包时间间隔平均值（(up_avg_ipt)是否很小（0.00001-0.001），单独判断是否为0
                    if (flows[i].up_avg_ipt <= 0.001 && flows[i].up_avg_ipt >= 0.00001)
                        bi[j].up_avg_iptExistSmall = true;
                    else if (flows[i].up_avg_ipt == 0)
                        bi[j].up_avg_iptExistZero = true;
                    //上行数据包最大时间间隔（up_max_ipt)是否为0
                    if (flows[i].up_max_ipt == 0)
                        bi[j].up_max_iptExistZero = true;
                    //下行数据包时间间隔平均值（(up_avg_ipt)是否很小（0.00001-0.001），单独判断是否为0
                    if (flows[i].dw_avg_ipt <= 0.001 && flows[i].dw_avg_ipt >= 0.00001)
                        bi[j].dw_avg_iptExistSmall = true;
                    else if (flows[i].dw_avg_ipt == 0)
                        bi[j].dw_avg_iptExistZero = true;
                    //下行数据包最大时间间隔（dw_max_ipt)是否为0
                    if (flows[i].dw_max_ipt == 0)
                        bi[j].dw_max_iptExistZero = true;

                    break;
                } else if ((flows[i].srcIp + '/' + flows[i].dstIp).equals(bi[j].src_dst_ip)) {//判断当前的对象的源IP+目的IP串是不是与读取到的流的串一样，一样的话流的个数+1
                    bi[j].flowNumber++;
                    bi[j].flow_sum_up_pl_bytes += flows[i].up_pl_bytes;
                    bi[j].flow_sum_duration += flows[i].duration;
                    bi[j].flow_sum_up_avg_ipt += flows[i].up_avg_ipt;
                    bi[j].flow_sum_dw_avg_ipt += flows[i].dw_avg_ipt;
                    bi[j].flow_sum_up_max_ipt += flows[i].up_max_ipt;
                    bi[j].flow_sum_dw_max_ipt += flows[i].dw_max_ipt;

                    //上行数据包时间间隔平均值（(up_avg_ipt)是否很小（0.00001-0.001），单独判断是否为0
                    if (flows[i].up_avg_ipt <= 0.001 && flows[i].up_avg_ipt >= 0.00001)
                        bi[j].up_avg_iptExistSmall = true;
                    else if (flows[i].up_avg_ipt == 0)
                        bi[j].up_avg_iptExistZero = true;
                    //上行数据包最大时间间隔（up_max_ipt)是否为0
                    if (flows[i].up_max_ipt == 0)
                        bi[j].up_max_iptExistZero = true;
                    //下行数据包时间间隔平均值（(up_avg_ipt)是否很小（0.00001-0.001），单独判断是否为0
                    if (flows[i].dw_avg_ipt <= 0.001 && flows[i].dw_avg_ipt >= 0.00001)
                        bi[j].dw_avg_iptExistSmall = true;
                    else if (flows[i].dw_avg_ipt == 0)
                        bi[j].dw_avg_iptExistZero = true;
                    //下行数据包最大时间间隔（dw_max_ipt)是否为0
                    if (flows[i].dw_max_ipt == 0)
                        bi[j].dw_max_iptExistZero = true;

                    if (!(flows[i].dataSet.equals(bi[j].dataset[k]))) {//判断我们读取到当前流的数据集名称是否与我们已经保存的数据集名称一样
                        bi[j].isTheSameDataset = false;                 //不一样的话，就将是否是同一数据集这个变量设置为false
                        int n;
                        for (n = 1; bi[j].dataset[n] != null && !bi[j].dataset[n].equals(bi[j].dataset[n - 1]); n++) {
                            //从1开始(因为在前面一个if里面已经给bi[j].dataset[0]赋值过了)，不一样的话将之后还是空的数据集变量赋上当前读取到的流的数据集的名称
                        }
                        bi[j].dataset[n] = flows[i].dataSet;
                        if (bi[j].dataset[n].equals(bi[j].dataset[n - 1]))
                            bi[j].dataset[n] = null;
                    }
                    break;
                }
            }
        }
        return bi;
    }

    //写入文件，以追加数据的方式写入
    private static void appendFile(String fileName, String content) {
        FileWriter writer = null;
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //readfile("C:/Users/Z/Desktop/operate_excel/test.xls");
        //System.out.println("- - - - - - - - - - - - -");
        Flow[] flows = readfile("C:/Users/Z/Desktop/operate_excel/apt_feature2.xlsx");//总数据是1533条，对象下标到1532
        Bundleip[] bundleips = countTheSameFlow(flows);
        File file = new File("C:/Users/Z/Desktop/operate_excel/", "apt.txt");
        try {
            file.createNewFile();//创建文件
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileName = file.getAbsolutePath();
        checkUp_pl_bytes(bundleips, flows);
        checkDuration(bundleips, flows);
        checkTheipt(bundleips, flows);
        for (int i = 0; i < BINUMB && bundleips[i].src_dst_ip != null; i++) {
            if (!bundleips[i].isTheSameDataset) {
                appendFile(fileName, bundleips[i].src_dst_ip + " 流的个数: " + bundleips[i].flowNumber + "    上行载荷总量平均值：" + bundleips[i].flow_sum_up_pl_bytes / bundleips[i].flowNumber
                        + "    持续时间平均值：" + bundleips[i].flow_sum_duration / bundleips[i].flowNumber + '\n');

                //"  平均值范围：" + bundleips[i].flow_sum_up_pl_bytes / bundleips[i].flowNumber * 0.8 + '-' + bundleips[i].flow_sum_up_pl_bytes / bundleips[i].flowNumber * 1.2
                //"  平均值范围：" + bundleips[i].flow_sum_duration / bundleips[i].flowNumber * 0.8+ '-' + bundleips[i].flow_sum_duration / bundleips[i].flowNumber * 1.2
                for (int j = 0; bundleips[i].dataset[j] != null; j++) {
                    appendFile(fileName, "存在不同的数据集，分别是：" + bundleips[i].dataset[j] + "  " + '\n');

                }
            } else if (bundleips[i].flowNumber > 1) {
                appendFile(fileName, bundleips[i].src_dst_ip + " 流的个数: " + bundleips[i].flowNumber + "    上行载荷总量平均值：" + bundleips[i].flow_sum_up_pl_bytes / bundleips[i].flowNumber
                        + "    持续时间平均值：" + bundleips[i].flow_sum_duration / bundleips[i].flowNumber + '\n');

            } else {
                appendFile(fileName, bundleips[i].src_dst_ip + " 流的个数: " + bundleips[i].flowNumber + '\n');

            }
        }
        appendFile(fileName, "————————————————————————————————分割线——————————————————————————————————————————————\n");

        for (int i = 0; i < BINUMB && bundleips[i].src_dst_ip != null; i++) {
            if (bundleips[i].up_pl_bytesAverageNumb / bundleips[i].flowNumber >= 0.8 && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的上行载荷总量是平均的, 平均范围内占比为：" + bundleips[i].up_pl_bytesAverageNumb / bundleips[i].flowNumber * 100 + '%' + '\n');
            else if (bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的上行载荷总量不是平均的" + '\n');
        }

        for (int i = 0; i < BINUMB && bundleips[i].src_dst_ip != null; i++) {
            if (bundleips[i].durationAverageNumb / bundleips[i].flowNumber >= 0.8 && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的持续时间是平均的, 平均范围内占比为：" + bundleips[i].durationAverageNumb / bundleips[i].flowNumber * 100 + '%' + '\n');
            else if (bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的持续时间不是平均的" + '\n');
        }
        appendFile(fileName, "————————————————————————————————分割线——————————————————————————————————————————————\n");
        for (int i = 0, k; i < BINUMB; i++) {
            k = 0;
            if (bundleips[i].up_avg_iptExistSmall) {
                appendFile(fileName, bundleips[i].src_dst_ip + " 的上行平均时间间隔存在很小值");
                k++;
            }
            if (bundleips[i].up_avg_iptExistZero) {
                if (0 == k) {
                    appendFile(fileName, bundleips[i].src_dst_ip + " 的上行平均时间间隔存在0");
                    k++;
                } else
                    appendFile(fileName, "    上行平均时间间隔存在0");
            }
            if (bundleips[i].up_max_iptExistZero) {
                if (0 == k) {
                    appendFile(fileName, bundleips[i].src_dst_ip + " 的上行最大时间间隔存在0");
                    k++;
                } else
                    appendFile(fileName, "    上行最大时间间隔存在0");
            }
            if (bundleips[i].dw_avg_iptExistSmall) {
                if (0 == k) {
                    appendFile(fileName, bundleips[i].src_dst_ip + " 的下行平均时间间隔存在很小值");
                    k++;
                } else
                    appendFile(fileName, "    下行平均时间间隔存在很小值");
            }
            if (bundleips[i].dw_avg_iptExistZero) {
                if (0 == k) {
                    appendFile(fileName, bundleips[i].src_dst_ip + " 的下行平均时间间隔存在0");
                    k++;
                } else
                    appendFile(fileName, "    下行平均时间间隔存在0");
            }
            if (bundleips[i].dw_max_iptExistZero) {
                if (0 == k) {
                    appendFile(fileName, bundleips[i].src_dst_ip + " 的下行最大时间间隔存在0");
                } else
                    appendFile(fileName, "    下行最大时间间隔存在0");
            }
            if (0 != k)
                appendFile(fileName, "\n");
        }
        appendFile(fileName, "———————————————————————————分割线———————————————————————————————————————");
        for (int i = 0; i < BINUMB; i++) {
            if (bundleips[i].up_avg_ipt_isSame && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的上行平均时间间隔一样\n");
            if (bundleips[i].dw_avg_ipt_isSame && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的下行平均时间间隔一样\n");
            if (bundleips[i].up_max_ipt_isSame && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的上行最大时间间隔一样\n");
            if (bundleips[i].dw_max_ipt_isSame && bundleips[i].flowNumber > 1)
                appendFile(fileName, bundleips[i].src_dst_ip + " 的下行最大时间间隔一样\n");
        }
        System.out.println("over");
    }


    // 计算上行载荷总量是否平均（80%的流处于平均范围（正负20%）就算平均）
    private static void checkUp_pl_bytes(Bundleip[] bi, Flow[] flow) {
        double[] avg = new double[BINUMB];
        for (int i = 0; i < BINUMB && bi[i].src_dst_ip != null; i++)
            avg[i] = bi[i].flow_sum_up_pl_bytes / bi[i].flowNumber;
        for (int i = 0; i < FLOWSUMNUM; i++) {
            int j = 0;
            while (bi[j].src_dst_ip != null) {
                if (!(flow[i].srcIp + '/' + flow[i].dstIp).equals(bi[j].src_dst_ip))
                    j++;
                else {
                    if (flow[i].up_pl_bytes >= avg[j] * 0.8 && flow[i].up_pl_bytes <= avg[j] * 1.2)
                        bi[j].up_pl_bytesAverageNumb++;
                    break;
                }
            }
        }

    }

    // 计算持续时间是否平均（80%的流处于平均范围（正负20%）就算平均）
    private static void checkDuration(Bundleip[] bi, Flow[] flow) {
        double[] avg = new double[BINUMB];
        for (int i = 0; i < BINUMB && bi[i].src_dst_ip != null; i++)
            avg[i] = bi[i].flow_sum_duration / bi[i].flowNumber;
        for (int i = 0; i < FLOWSUMNUM; i++) {
            int j = 0;
            while (bi[j].src_dst_ip != null) {
                if (!(flow[i].srcIp + '/' + flow[i].dstIp).equals(bi[j].src_dst_ip))
                    j++;
                else {
                    if (flow[i].duration >= avg[j] * 0.8 && flow[i].duration <= avg[j] * 1.2)
                        bi[j].durationAverageNumb++;
                    break;
                }
            }
        }
    }


    //上下行数据包最大时间间隔和平均时间间隔是否一样
    private static void checkTheipt(Bundleip[] bi, Flow[] flow) {
        double[] up_avg_averge = new double[BINUMB];
        double[] dw_avg_averge = new double[BINUMB];
        double[] up_max_averge = new double[BINUMB];
        double[] dw_max_averge = new double[BINUMB];
        //Arrays.fill(avg, 0.0);
        for (int i = 0; i < BINUMB && bi[i].src_dst_ip != null; i++) {
            up_avg_averge[i] = bi[i].flow_sum_up_avg_ipt / bi[i].flowNumber;
            dw_avg_averge[i] = bi[i].flow_sum_dw_avg_ipt / bi[i].flowNumber;
            up_max_averge[i] = bi[i].flow_sum_up_max_ipt / bi[i].flowNumber;
            dw_max_averge[i] = bi[i].flow_sum_dw_max_ipt / bi[i].flowNumber;
        }
        for (int i = 0; i < FLOWSUMNUM; i++) {
            int j = 0;
            while (bi[j].src_dst_ip != null) {
                if (!(flow[i].srcIp + '/' + flow[i].dstIp).equals(bi[j].src_dst_ip))
                    j++;
                else {
                    bi[j].up_avg_ipt_isSame = (Math.abs(flow[i].up_avg_ipt - up_avg_averge[j]) < 0.1);
                    bi[j].dw_avg_ipt_isSame = (Math.abs(flow[i].dw_avg_ipt - dw_avg_averge[j]) < 0.1);
                    bi[j].up_max_ipt_isSame = (Math.abs(flow[i].up_max_ipt - up_max_averge[j]) < 0.1);
                    bi[j].dw_max_ipt_isSame = (Math.abs(flow[i].dw_max_ipt - dw_max_averge[j]) < 0.1);
                    break;
                }
            }
        }
    }

    private static Flow[] readfile(String filename) {//将读取的结果作为一个数组对象返回
        boolean isE2007 = false;
        if (filename.endsWith("xlsx"))
            isE2007 = true;
        Flow[] flow = new Flow[FLOWSUMNUM];
        try {
            InputStream input = new FileInputStream(filename);//建立输入流
            Workbook wb;
            //根据文件格式来初始化
            if (isE2007)
                wb = new XSSFWorkbook(input);
            else
                wb = new HSSFWorkbook(input);
            Sheet sheet = wb.getSheetAt(0);//获得第一个表单；
            Iterator<Row> rows = sheet.rowIterator();//获得第一个表单的迭代器；
            int i = 0;
            Row row;
            row = rows.next();
            while (rows.hasNext()) {
                row = rows.next();//获取行内数据
                Iterator<Cell> cells = row.cellIterator();//获得第一行的迭代器
                flow[i] = new Flow();
                int j = 0;//来计数每一行的单元格，j从0开始，单元格从第一个开始
                while (cells.hasNext()) {
                    Cell cell = cells.next();//获得行数据
                    switch (j) {           //将不同单元格的数据存放到对象的不同变量里面去
                        case 0:                                             //j==0表示每一行的第一个单元格，即数据集名称
                            if (cell.getCellType() == 1)
                                flow[i].setDataSet(cell.getStringCellValue());
                            else if (cell.getCellType() == 3)
                                flow[i].setDataSet(flow[i - 1].dataSet);    //根据excel表格，这里读到空的话就依据前一行的名称
                            break;
                        case 1:                                             //j==1表示每一行的第二个单元格，即源IP
                            flow[i].setSrcIp(cell.getStringCellValue());
                            break;
                        case 2:
                            flow[i].setDstIp(cell.getStringCellValue());
                            break;
                        case 3:
                            flow[i].setUp_pl_bytes(cell.getNumericCellValue());
                            break;
                        case 4:
                            flow[i].setDuration(cell.getNumericCellValue());
                            break;
                        case 6:
                            flow[i].setUp_avg_ipt(cell.getNumericCellValue());
                            break;
                        case 7:
                            flow[i].setDw_avg_ipt(cell.getNumericCellValue());
                            break;
                        case 8:
                            flow[i].setUp_max_ipt(cell.getNumericCellValue());
                            break;
                        case 9:
                            flow[i].setDw_max_ipt(cell.getNumericCellValue());
                            break;
                        default:
                            //System.out.println("unsuported sell type ");
                            break;
                    }
                    j++;                                                    //j++，表示指向下一个单元格
                }
                i++;                                                        //i++，准备开始下一个对象的数据存储
            }
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flow;
    }

}
