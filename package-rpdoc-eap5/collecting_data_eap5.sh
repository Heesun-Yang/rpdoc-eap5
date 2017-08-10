rm -f data/services_result.json
java -Dlogfile=collecting_data.log -cp rpdoc-eap5.jar com.rp.docs.eap5.collecting_data.CollectingDataEap5 
