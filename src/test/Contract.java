package test;

public class Contract {
	private Long id;
	
	/**
	 *合同名称
	 */
	private String name;
	
	
	/**
	 * 合同编号
	 */
	private String contractNo;
	
	

	/**
	 *  合同归档扫描件关联
	 */
	private  Long archiveSubRefrence;
	
	/**
	 * 合同盖章扫描件
	 */
	private  Long stampSubRefrence;


	

	public Long getStampSubRefrence() {
		return stampSubRefrence;
	}



	public void setStampSubRefrence(Long stampSubRefrence) {
		this.stampSubRefrence = stampSubRefrence;
	}



	public Long getArchiveSubRefrence() {
		return archiveSubRefrence;
	}



	public void setArchiveSubRefrence(Long archiveSubRefrence) {
		this.archiveSubRefrence = archiveSubRefrence;
	}



	public String getContractNo() {
		return contractNo;
	}



	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	


	




	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	


	
	
	
	

}
