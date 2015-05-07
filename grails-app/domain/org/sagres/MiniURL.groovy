package org.sagres

class MiniURL
{

	static constraints = {
	}

	String miniURL
	String fullURLHash
	String fullURL
	String args
	String controller
	String action
	Date dateCreated

	// this is the ID to use for the link being called (would normally be equal to params.id of the newly reconstructed item)
	String paramsID

  static mapping = {
    fullURL type:"text"
    args type:"text"
  }
}
