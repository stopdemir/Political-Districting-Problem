import ilog.concert.IloException;


import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

//apache excel read libraries
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MathFormulationPDwithout {

	public static void solveMe(int n, int m, int p, int obje) throws IOException {

		double[][] arcs = new double[2 * n + 1][2 * n + 1];
		arcs = readArcData(n);

		double[][] v = new double[n][p];
		v = readElectionData(n, p);

		double[] po = new double[n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < p; j++) {
				po[i] = po[i] + v[i][j];
			}
		}

		float bigM = 100000000;
		double Beta = 0.10;

		double sum = 0;
		for (double value : po) {
			sum += value;
		}
		double sBar = sum / m;

		// System.out.println(sBar);
		// model
		try {
			IloCplex cplex = new IloCplex();

			// Variables
			IloNumVar[][] y = new IloNumVar[n][]; // boolean
			for (int i = 0; i < n; i++) {
				y[i] = cplex.boolVarArray(m);
			}

			IloNumVar[][] z = new IloNumVar[n][]; // boolean
			for (int i = 0; i < n; i++) {
				z[i] = cplex.boolVarArray(m);
			}

			//double[][] zValues = new double[n][m];
			//zValues = readInitialData(n, m);

			// IloNumVar[]t=cplex.numVarArray(m,0,Double.MAX_VALUE);
			// IloNumVar[] startVar_z = new IloNumVar[n * m];
			
			/*
			IloNumVar[] startVar_z = cplex.boolVarArray(n * m);
			double[] startVal_z = new double[n * m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					startVar_z[i * m + j] = z[i][j];
					startVal_z[i * m + j] = zValues[i][j];
				}
			}*/

			IloNumVar[][][] dum = new IloNumVar[p][][];
			for (int i = 0; i < p; i++) {
				dum[i] = new IloNumVar[p][];
				for (int j = 0; j < p; j++) {
					dum[i][j] = cplex.boolVarArray(m);
				}
			}

			IloNumVar[][][] flow = new IloNumVar[2 * n + 1][][];
			for (int i = 0; i < 2 * n + 1; i++) {
				flow[i] = new IloNumVar[2 * n + 1][];
				for (int j = 0; j < 2 * n + 1; j++) {
					flow[i][j] = cplex.numVarArray(m, 0, Double.MAX_VALUE);
				}
			}

			IloNumVar[][] c = new IloNumVar[p][]; // boolean
			for (int i = 0; i < p; i++) {
				c[i] = cplex.boolVarArray(m);
			}

			IloNumVar[][] o = new IloNumVar[p][];
			for (int i = 0; i < p; i++) {
				o[i] = cplex.numVarArray(m, 0, Double.MAX_VALUE);
			}

			IloNumVar[] t = cplex.numVarArray(m, 0, Double.MAX_VALUE);

			// objective
			// 0 is for AKP, 1 is CHP or YES;NO respectively
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int h = 0; h < m; h++) {
				obj.addTerm(c[obje][h], 1);
			}
			cplex.addMaximize(obj);

			// THE CONSTRAINTS
			// From old models
			

			//forall(h in Zone, k in Party) sum(i in District) z[i][h]*v[i][k]==o[k][h];
			// 4*5=20
	
			for (int h = 0; h < m; h++) {
				for (int k = 0; k < p; k++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						expr1.addTerm(z[i][h], v[i][k]);
					}
					cplex.addEq(expr1, o[k][h]);
				}
			}
			

			//forall(h in Zone, k in Party, p in Party) o[p][h]-o[k][h]+bigM*dum[k][p][h]>=0;

			// same party problem can arise 4*5*5=100 +20=120
			for (int h = 0; h < m; h++) {
				for (int k = 0; k < p; k++) {
					for (int p1 = 0; p1 < p; p1++) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						expr1.addTerm(1, o[p1][h]);
						expr1.addTerm(-1, o[k][h]);
						expr1.addTerm(bigM, dum[k][p1][h]);
						cplex.addGe(expr1, 0);
					}
				}
			}
			// 4*
			//forall(h in Zone, k in Party) sum(p in Party:k!=p) dum[k][p][h]-numParties+2<=c[k][h];

			for (int h = 0; h < m; h++) {
				for (int k = 0; k < p; k++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					for (int p1 = 0; p1 < p; p1++) {
						if (p1 != k) {
							expr1.addTerm(1, dum[k][p1][h]);
							expr1.addTerm(-1, c[k][h]);
						}
					}
					cplex.addLe(expr1, p - 2);
				}
			}
			//forall(h in Zone) sum(k in Party) c[k][h]==1; 

			for (int h = 0; h < m; h++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int k = 0; k < p; k++) {
					expr1.addTerm(1, c[k][h]);
				}
				cplex.addEq(expr1, 1);
			}
			
			//forall(h in Zone) sum(i in District) z[i][h]*p[i]==t[h];

			for (int h = 0; h < m; h++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int i = 0; i < n; i++) {
					expr1.addTerm(z[i][h], po[i]);
				}
				cplex.addEq(expr1, t[h]);
			}


			//forall(h in Zone) t[h]<=sBar*(1+Beta);

			for (int h = 0; h < m; h++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				expr1.addTerm(1, t[h]);
				cplex.addLe(expr1, sBar * (1 + Beta));
			}
			//forall(h in Zone) t[h]>=sBar*(1-Beta);

			for (int h = 0; h < m; h++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				expr1.addTerm(1, t[h]);
				cplex.addGe(expr1, sBar * (1.0 - Beta));
			}

			// forall(i in District, h in Zone) //parametere'nin 0 da olmasý ile
			// alakalý sorun çýkabilir arcs[0][2] gibi'
			// sum(j in DistrictAll:i!=j) flow[j][i][h]*arcs[j][i]==sum(j in
			// DistrictAll:i!=j) flow[i][j][h]*arcs[i][j];

			for (int i = 0; i < n; i++) {
				for (int h = 0; h < m; h++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int j = 0; j < 2 * n + 1; j++) {
						if (i != j) {
							expr1.addTerm(arcs[j][i], flow[j][i][h]);
							expr2.addTerm(arcs[i][j], flow[i][j][h]);
						}
					}
					cplex.addEq(expr1, expr2);
				}
			}
			// forall(i in District, h in Zone) flow[0][i][h]==200*y[i][h];

			for (int i = 0; i < n; i++) {
				for (int h = 0; h < m; h++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					expr1.addTerm(1, flow[2 * n][i][h]);
					expr1.addTerm(-200, y[i][h]);
					cplex.addEq(expr1, 0);
				}
			}

			// forall(h in Zone) sum(i in District) y[i][h]==1;
			for (int h = 0; h < m; h++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int i = 0; i < n; i++) {
					expr1.addTerm(1, y[i][h]);
				}
				cplex.addEq(expr1, 1);
			}

			// forall(i in District, h in Zone) sum(j in DistrictAll:i!=j)
			// flow[j][i][h]*arcs[j][i]<=200*z[i][h];
			// parameter arcs
		
			for (int i = 0; i < n; i++) {
				for (int h = 0; h < m; h++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int j = 0; j < 2 * n + 1; j++) {
						if (j != i) {
							expr1.addTerm(flow[j][i][h], arcs[j][i]);
						}
					}
					expr2.addTerm(200, z[i][h]);
					cplex.addLe(expr1, expr2);
				}
			}
			
			// forall(i in District, h in Zone)
			// z[i][h]<=flow[i][i+numDistricts][h];
         
			for (int i = 0; i < n; i++) {
				for (int h = 0; h < m; h++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					expr1.addTerm(1, z[i][h]);
					cplex.addLe(expr1, flow[i][i + n][h]);
				}
			}
			
			// forall(i in District) sum(h in Zone) z[i][h]==1;
		
			for (int i = 0; i < n; i++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int h = 0; h < m; h++) {
					expr1.addTerm(1, z[i][h]);
				}
				cplex.addEq(expr1, 1);
			}
			


            cplex.setParam(IloCplex.Param.TimeLimit, 7200);
			
			
			cplex.solve();
			cplex.setParam(IloCplex.Param.Output.WriteLevel, 1);

			double objVal = cplex.getObjValue();
			System.out.println("obj value:" + objVal);
			double[] _t = cplex.getValues(t);
			// double [][] _y=cplex.getValues(y);
			System.out.println("t= " + Arrays.toString(_t));
			
			double [][] cStar=new double [p][m];
			for( int i = 0; i < p; i++){
				for (int g = 0; g < m; g++){
					cStar[i][g] = cplex.getValue(c[i][g]);
				}
			}
			
			double [][] zStar=new double [n][m];
			for( int i = 0; i < n; i++){
				for (int g = 0; g < m; g++){
					zStar[i][g] = cplex.getValue(z[i][g]);
				}
			}
			
			//double[] _z = cplex.getValues(startVar_z);
			//System.out.println("assignment values= " + Arrays.toString(_z));


			
			System.out.println("output z values");
			System.out.print(Arrays.deepToString(zStar));    
			   
			String fileName="C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\Resultssss.xlsx";
			exportDataToExcel(fileName, zStar);
			

			// cplex.exportModel("example.lp");
			cplex.writeSolution("example2.sol");
			// cplex.writeSolution("example.mst");
			// cplex.writeSolution("example.xml");
			// cplex.setParam(IloCplex.writeSolution, 2);
			// cplex.end();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	
	public static double[][] readElectionData(int numberOfUnits, int numberOfParties) throws IOException {


		String selectedFile ="C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\UskudarUmraniye_Referendum.xlsx";
		//String selectedFile ="C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\Uskudar_Referendum.xlsx";
		FileInputStream fis = new FileInputStream(selectedFile);
		// create workbook instance that refers to.xlsx file
		Workbook workbook = new XSSFWorkbook(fis);
		// workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_NULL_AND_BLANK);
		Sheet firstsheet = workbook.getSheetAt(0);
		double electionData[][] = new double[numberOfUnits][numberOfParties];
		// int arcData[][] = new int[numberOfUnits][numberOfParties];

		for (int i = 0; i < numberOfUnits; i++) {
			Row row = firstsheet.getRow(i);
			for (int j = 0; j < numberOfParties; j++) {
				if (row.getCell(j) == null) {
					row.createCell(j);
				} else {
					electionData[i][j] = (int) row.getCell(j).getNumericCellValue();
				}
			}
		}
		workbook.close();
		return electionData;
	}

	public static double[][] readArcData(int numberOfUnits) throws IOException {

		String selectedFile ="C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\UskudarUmraniye_Referendum.xlsx";
		//String selectedFile ="C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\Uskudar_Referendum.xlsx";
		FileInputStream fis = new FileInputStream(selectedFile);
		Workbook workbook2 = new XSSFWorkbook(fis);
		// workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_NULL_AND_BLANK);
		Sheet secondsheet = workbook2.getSheetAt(1);
		double arcData[][] = new double[1 + (numberOfUnits * 2)][1 + (numberOfUnits * 2)];

		for (int i = 0; i < 1 + (numberOfUnits * 2); i++) {
			Row row = secondsheet.getRow(i);
			for (int j = 0; j < 1 + (numberOfUnits * 2); j++) {
				if (row.getCell(j) == null) {
					row.createCell(j);
				} else {
					arcData[i][j] = (int) row.getCell(j).getNumericCellValue();
				}
			}
			workbook2.close();
		}
		return arcData;
	}

	public static double[][] readInitialData(int numberOfUnits, int numberOfZones) throws IOException {

		String selectedFile = "C:\\Users\\labuser\\eclipse_workspace\\ProjectPD\\resultsPage.xlsx";
		FileInputStream fis = new FileInputStream(selectedFile);
		// create workbook instance that refers to.xlsx file
		Workbook workbook = new XSSFWorkbook(fis);
		// workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_NULL_AND_BLANK);
		Sheet firstsheet = workbook.getSheetAt(0);
		double initialData[][] = new double[numberOfUnits][numberOfZones];
		// int arcData[][] = new int[numberOfUnits][numberOfParties];

		for (int i = 0; i < numberOfUnits; i++) {
			Row row = firstsheet.getRow(i);
			for (int j = 0; j < numberOfZones; j++) {
				if (row.getCell(j) == null) {
					row.createCell(j);
				} else {
					initialData[i][j] = (int) row.getCell(j).getNumericCellValue();
				}
			}
		}
		workbook.close();
		return initialData;
	}
	
    public static void exportDataToExcel(String fileName, double[][] data) throws FileNotFoundException, IOException
    {
        File file = new File(fileName);
        if (!file.isFile())
            file.createNewFile();

		//Create Excel Instance
		 XSSFWorkbook workbook = new XSSFWorkbook();
		 //Create Sheet named as Data
		 XSSFSheet sheet = workbook.createSheet("Data");
        //CSVWriter csvWriter = new CSVWriter(new FileWriter(file));

        int rowCount = data.length;

        for (int i = 0; i < rowCount; i++)
        {
            int columnCount = data[i].length;
            XSSFRow row = sheet.createRow(i);
            
            for (int j = 0; j < columnCount; j++)
            {
            	double values = data[i][j];
            	XSSFCell cell = row.createCell(j);
                cell.setCellValue(values);
            }
        }
		FileOutputStream fileout = new FileOutputStream(new File(fileName));
		workbook.write(fileout);
		fileout.close();
    }

	/*
	 * String[] MaterialsNameX = new String[altMalzemeX];
	 * 
	 * DataFormatter formatter = new DataFormatter();
	 * 
	 * for (int i = 0; i < altMalzemeX; i++) { row = firstsheet.getRow(i);
	 * MaterialsNameX[i] = formatter.formatCellValue(row.getCell(0));
	 * MaterialsName.add(MaterialsNameX[i]); }
	 * 
	 * for (int i = 0; i < altMalzemeX; i++) { for (int j = 0; j < üstMalzemeX -
	 * 2; j++) { newData[i][j] = Data[i][j + 2]; } }
	 */

}
