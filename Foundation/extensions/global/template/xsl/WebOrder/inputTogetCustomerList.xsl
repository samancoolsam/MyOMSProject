<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="Order">
      <Customer>
         <CustomerContactList>
            <CustomerContact EmailID="{@CustomerEMailID}">
            </CustomerContact>
         </CustomerContactList>
      </Customer>
   </xsl:template>
</xsl:stylesheet>

