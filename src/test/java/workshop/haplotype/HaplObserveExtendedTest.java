/**
 * This is a test case for DriverForHaplObserveExtended
 */
package workshop.haplotype;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import workshop.haplotype.collective.AddFamRelationCountry;
import workshop.haplotype.collective.CombineGLFile;
import workshop.haplotype.collective.FamSamRelationCountry;
import workshop.haplotype.collective.SeparateHapAndAllele;
import workshop.haplotype.frequency.ranking.HapTarget;
import workshop.haplotype.frequency.write.GenerateCountryHapGLstring;
import workshop.haplotype.frequency.write.GenerateGlobalGroupsHapCountTable;
import workshop.haplotype.frequency.write.GenerateHapFrequencyTableForHLAHapV;
import workshop.haplotype.organize.file.CreateDirectoryList;
import workshop.haplotype.write.GenerateFamSampleRelationHapValidCountryTable;
import workshop.haplotype.write.GenerateHaplotypeGLStringSummary;
import workshop.haplotype.write.GenerateHaplotypeSummary;
import workshop.haplotype.write.RunHaplObserveForMultipleFamilies;

/**
 * @author kazu
 * @version June 19 2018
 *
 */
public class HaplObserveExtendedTest extends TestCase {
	private static final DateFormat dateFormat;
	private static final Date date;		
	private static final String today;	
	private static final String global = "src/test/resources/";
	private static final String collective = global + "collective/";
	private static final String haplotype = collective + "haplotype/";
	private static final CombineGLFile combined;
	private static final FamSamRelationCountry fsrc;
	
	static {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		date = new Date();	
		today = dateFormat.format(date);
		combined = new CombineGLFile(collective);
		fsrc = new FamSamRelationCountry(combined);
	}
	
	@Test
	public void testHaplObserveExtended() {
		//testing core of HaplObserve, but testing multiple families
		String famcsv = collective+ "FAMCSV/";
		new RunHaplObserveForMultipleFamilies(collective, combined, famcsv, haplotype, today);
		
		
		File hap = new File(haplotype);
		for (File file : hap.listFiles()) {
			if (file.isDirectory()) {
				String fam = file.getAbsolutePath();
				File glFile = new File(fam);
				for (File test : glFile.listFiles()) {
					if (test.getName().contains("Validation")) {
						System.out.println(test.getAbsolutePath());
						SeparateHapAndAllele saa = new SeparateHapAndAllele(test.getAbsolutePath(), fsrc);
						for (List<String> list : saa.getFamSamRelationHapCountry()) {																
							for (int count = 0; count < 2; count++) {	// family, sample, relation
								for (int index = 0; index < 3; index++) {
									System.out.println(list.get(index) + ",");										
								}
								for (String type : saa.getSampleAllele().get(list.get(1)).get(count)) {
									System.out.println(type + ",");
								}
								System.out.println(list.get(4) + ",");	// validation
								System.out.println(list.get(5) + "\n");	// country
							}							
						}	
					}
				}
			}
		}
		
		
		// generate table that is easily reviewed by human
		// per family table
		new GenerateFamSampleRelationHapValidCountryTable(haplotype, "Validation", fsrc,
						"_Haplotype_Summary_Table_" + today + ".csv");		
		// combined summary table
		new GenerateHaplotypeSummary(haplotype, "Validation", fsrc, today);	
		

		for (File file : hap.listFiles()) {
			if (file.isDirectory()) {
				String fam = file.getAbsolutePath();
				File glFile = new File(fam);
				for (File test : glFile.listFiles()) {					
					if (test.getName().contains("Validation")) {
						System.out.println(test.getAbsolutePath());
						AddFamRelationCountry arc = new AddFamRelationCountry(test.getAbsolutePath(), fsrc);
						for (List<String> list : arc.getFamSamRelationHapCountry()) {
							System.out.println(list);
						}	
					}
				}
			}
		}	
		
		
		// generate GL String table
		// this is for haplotype frequency
		
		// TODO:  Revisit - changing to haplotype dir
		//new GenerateHaplotypeGLStringSummary(haplotype, collective , "Validation", fsrc, today);
		new GenerateHaplotypeGLStringSummary(haplotype, haplotype , "Validation", fsrc, today);
		new GenerateHaplotypeGLStringSummary(haplotype, haplotype, "SingleAlleleHapType", fsrc, today);
		new GenerateHaplotypeGLStringSummary(haplotype, haplotype, "TwoFieldAlleleHapType", fsrc, today);	
		
		
		// haplotype frequency
		// input files are generated above steps
		// separate by ethnic groups or countries
		
		// INFO:  CHanging this to the haplotype directory to explicitly separate input files from generated output files - so that output files are not checked into source control
		// TODO: double-check
		//new GenerateCountryHapGLstring(global, 
		//			collective + "FAM_Haplotype_Summary_GL_String_" + today + ".csv", fsrc, "FAM", today);
		
		new GenerateCountryHapGLstring(global, 
					haplotype + "FAM_Haplotype_Summary_GL_String_" + today + ".csv", fsrc, "FAM", today);
		new GenerateCountryHapGLstring(global, 
						haplotype + "SingleAlleleHapType_Haplotype_Summary_GL_String_" + today + ".csv", 
						fsrc, "SingleAllele", today);
		new GenerateCountryHapGLstring("src/test/resources/", 
						haplotype + "TwoFieldAlleleHapType_Haplotype_Summary_GL_String_" + today + ".csv",
						fsrc, "TwoFieldAllele", today);
		
		
		
		CreateDirectoryList cdl = new CreateDirectoryList(global);
		for (String str : cdl.getDirList()) {
			System.out.println(str);
		}
		
		for (String str : cdl.getInputFileNameList()) {
			System.out.println(str);
		}	
		System.out.println(cdl.getGlobal());
		
		
		
		HapTarget ht = new HapTarget();		// target haplotype		
		new File(global + "summary").mkdir();	// make FAMCSV dir
		for (int index = 0; index < ht.getHapTargetList().size(); index++) {	// go through target	
			String output = global + "summary/Global_" + ht.getNameList().get(index) + "_Haplotype_Summary_" + today + ".csv";
			new GenerateGlobalGroupsHapCountTable(global, 
					ht.getHapTargetList().get(index), output);
			
			// HLAHapV format
			new GenerateHapFrequencyTableForHLAHapV(global, ht.getHapTargetList().get(index), 
					ht.getNameList().get(index), today);			
		}
		
	}
	



	

	
	
	


}