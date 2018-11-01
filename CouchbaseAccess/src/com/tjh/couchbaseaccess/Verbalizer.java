package com.tjh.couchbaseaccess;

public class Verbalizer {
	
	public Boolean toggle = false;

	public Verbalizer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Method to verbalize rainfall predictions.
	 * 
	 */
	public String verbalizeLikelihoodOfRain(int highestPpValue) {
		String returnString = "";
		
		if (highestPpValue < 20) {
			returnString = returnString + "Chances of rain are quite low for";
		} else {
			if (highestPpValue > 19 && highestPpValue < 40) {
				returnString = returnString + "Rain is a possibility for";
			} else {
				if (highestPpValue > 29 && highestPpValue < 60) {
					returnString = returnString + "Chances or rain are about even for";
				} else {
					if (highestPpValue > 59 && highestPpValue < 80) {
						
						if ((highestPpValue % 2) == 0) {
							returnString = returnString + "Chances of rain are fairly high for";
							
						} else {
							returnString = returnString + "Rain is quite likely for";

						}

					} else {
						returnString = returnString + "Rain is extremely likely for";
					}
				}
			}
		}
		
		if (returnString == "") {
			returnString = "Could not determine the chances of rain for";
		}		
		
		return returnString;
	}
	
	/**
	 * Method to verbalize degree of need for contingency item.
	 * 
	 */
	public String verbalizeDegreeOfNeedForContingencyItem(int highestPpValue, String contingencyMode) {
		String returnString = "";
		
		if (highestPpValue < 20) {
			returnString = returnString + " So you may not need your ";
		} else {
			if (highestPpValue > 19 && highestPpValue < 40) {
				returnString = returnString + " So you may need your ";
			} else {
				if (highestPpValue > 29 && highestPpValue < 60) {
					returnString = returnString + " So you may want to " + contingencyMode + " your ";
				} else {
					if (highestPpValue > 59 && highestPpValue < 80) {
						returnString = returnString + " So yes, you had better " + contingencyMode + " your ";
					} else {
						returnString = returnString + " So you should definitely " + contingencyMode + " your ";
					}
				}
			}
		}
		
		if (returnString == "") {
			returnString = "Could not determine degree of need for contingency item";
		}		
		
		return returnString;
	}
	
	/**
	 * Method to verbalize degree of need for liability item.
	 * 
	 */
	public String verbalizeDegreeOfAvoidanceForLiabilityItem(int highestPpValue) {
		String returnString = "";
		
		if (highestPpValue < 20) {
			returnString = returnString + " So you should be able to ";
		} else {
			if (highestPpValue > 19 && highestPpValue < 40) {
				returnString = returnString + " So it might not be a bad idea to ";
			} else {
				if (highestPpValue > 29 && highestPpValue < 60) {
					returnString = returnString + " So you may not want to ";
				} else {
					if (highestPpValue > 59 && highestPpValue < 80) {
						returnString = returnString + " So you probably shouldn't ";
					} else {
						returnString = returnString + " So you definitely shouldn't ";
					}
				}
			}
		}
		
		if (returnString == "") {
			returnString = "Could not determine degree of need for liability item";
		}		
		
		return returnString;
	}
	/**
	 * Method to verbalize degree of need for liability item.
	 * 
	 */
	public String verbalizePossibilityOfNegativeEffectItem(int highestPpValue) {
		String returnString = "";
		
		if (highestPpValue < 20) {
			returnString = returnString + " Not likely to be ";
		} else {
			if (highestPpValue > 19 && highestPpValue < 40) {
				returnString = returnString + " May be somewhat ";
			} else {
				if (highestPpValue > 29 && highestPpValue < 60) {
					returnString = returnString + " Could be ";
				} else {
					if (highestPpValue > 59 && highestPpValue < 80) {
						returnString = returnString + " Might we be ";
					} else {
						returnString = returnString + " Probably ";
					}
				}
			}
		}
		
		if (returnString == "") {
			returnString = "Could not determine effect item";
		}		
		
		return returnString;
	}
	
	/**
	 * Method to verbalize degree of need for liability item.
	 * 
	 */
	public String verbalizePossibilityOfPositiveEffectItem(int highestPpValue) {
		String returnString = "";
		
		if (highestPpValue < 20) {
			returnString = returnString + " Should be ";
		} else {
			if (highestPpValue > 19 && highestPpValue < 40) {
				returnString = returnString + " May be ";
			} else {
				if (highestPpValue > 29 && highestPpValue < 60) {
					returnString = returnString + " May not be ";
				} else {
					if (highestPpValue > 59 && highestPpValue < 80) {
						returnString = returnString + " Probably not ";
					} else {
						returnString = returnString + " No, not ";
					}
				}
			}
		}
		
		if (returnString == "") {
			returnString = "Could not determine effect item";
		}		
		
		return returnString;
	}
}
