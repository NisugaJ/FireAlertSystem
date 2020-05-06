/**
 * 
 */
package models;

/**
 * @author IT18117110( Jayawardana N.S | nisuga.rockwell@gmail.com )
 *
 */
public class ClientProcess {
	private Process process;
	private Boolean started;

	/**
	 * 
	 */
	public ClientProcess() {
	}

	public ClientProcess(Process process, Boolean started) {
		super();
		this.process = process;
		this.started = started;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public Boolean getStarted() {
		return started;
	}

	public void setStarted(Boolean started) {
		this.started = started;
	}

}
