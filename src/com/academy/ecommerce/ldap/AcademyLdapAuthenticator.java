package com.academy.ecommerce.ldap;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.academy.util.logger.Logger;
import com.academy.util.constants.AcademyConstants;
import com.yantra.shared.ycp.YCPErrorCodes;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.util.YFSAuthenticator;

//Flag-Based
public class AcademyLdapAuthenticator implements YFSAuthenticator {
            
            private static Logger logger = Logger
                                    .getLogger(AcademyLdapAuthenticator.class.getName());

            public Map authenticate(String sLoginID, String sPassword) throws Exception {
                        
                        logger.verbose("Inside method : authenticate()");
                        /*
                        * from email
            				env.put(Context.PROVIDER_URL, "ldaps://ldapad.academy.com:636/"); 
            				env.put(Context.SECURITY_PRINCIPAL, "CN=apadsterling,OU=Service,OU=UserAccounts,DC=academy,DC=com"); 
							env.put(Context.SECURITY_CREDENTIALS, "<account password>"); 
                        */
                        
                        if (logger.isVerboseEnabled()) {
                                    logger.verbose("Parameters" + ":" + sLoginID);
                        }
                        logger.debug("1");
                        String ldapFactory = YFSSystem.getProperty("yfs.security.ldap.factory");
                        String adminUserDN;
                		String adminUserCredentials;
                		String ldapURL;
                		if (!YFCObject.isVoid(YFSSystem.getProperty("academy.ldap.activedirectory.enabled")) && 
                				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty("academy.ldap.activedirectory.enabled"))) {
                			//Active Directory Authentication
                			logger.debug("inside if loop if true");
                            //String adminUserDN = "cn=apadsterling,ou=UserAccounts,o=Academy,o=com";                       
                            adminUserDN = "cn=apadsterling,ou=Service,ou=UserAccounts,dc=Academy,dc=com";
                            logger.debug("adminUserDN::::"+adminUserDN);
                            adminUserCredentials = "sw9pAViS";
                            logger.debug("adminUserCredentials::::"+adminUserCredentials);
                            ldapURL = YFSSystem.getProperty("yfs.security.ldap.ad.url");
                            logger.debug("ldapURL"+ldapURL);
                		} else {
                			//eDirectory Authentication
                			logger.debug("inside else loop if false");
                			adminUserDN = "cn=adminapps,ou=accounts,o=academy";
                			logger.debug("adminUserDN : "+ adminUserDN);
                			adminUserCredentials = "adminapps";
                			logger.debug("adminUserCredentials : "+ adminUserCredentials);
                            ldapURL = YFSSystem.getProperty("yfs.security.ldap.ed.url");
                            logger.debug("ldapURL"+ldapURL);

                		}

                        YFSException yfe = new YFSException();
                        // if any of the ldap params are not set, throw exception
                        if (YFCObject.isVoid(ldapURL) || YFCObject.isVoid(ldapFactory)) {
                                    logger.debug("inside ldapURL line no 47");
                                    YFCException ex = new YFCException(
                                                YCPErrorCodes.YCP_INVALID_LDAP_AUTHENTICATOR_CONFIGURATION);
                                    ex.setAttribute("yfs.security.ldap.factory", ldapFactory);
                                    logger.debug("after setattribute");
                                    ex.setAttribute("yfs.security.ldap.url", ldapURL);
                                    throw ex;
                        } else {
                                    logger.debug("inside else");
                                    Hashtable env = new Hashtable();
                                    env.put(Context.INITIAL_CONTEXT_FACTORY, ldapFactory);
                                    env.put(Context.SECURITY_AUTHENTICATION, "simple");
                                    env.put(Context.PROVIDER_URL, ldapURL);
                                    env.put(Context.SECURITY_PRINCIPAL, adminUserDN);
                                    env.put(Context.SECURITY_CREDENTIALS, adminUserCredentials);
                                    env.put(Context.SECURITY_PROTOCOL, "ssl");

                                    logger.verbose("printing env -hastable: " + env);

                                    logger.verbose("LDAP URL: " + ldapURL);

                                    DirContext ctx = null;
                                    DirContext userCtx = null;
                                    logger.debug("4");

                                    try {
                                                // Create the initial context for admin binding
                                                logger.debug("inside try");

                                                ctx = new InitialDirContext(env);
                                                logger.verbose(">>>>>>>>>>"
                                                                        + (ctx.getEnvironment().get(Context.PROVIDER_URL))
                                                                                                .toString());
                                                logger.verbose("ctx&&&&&&&&&&&&&"+ctx);
                                                SearchControls ctls = new SearchControls();
                                                ctls.setSearchScope(ctls.SUBTREE_SCOPE);
                                                logger.verbose("ctls.getSearchScope() >>>>>>>>>>"+ ctls.getSearchScope());
                                                logger.verbose("ctls"+ctls);
                                                logger.verbose("Admin App Authentication Success");

                                                NamingEnumeration<SearchResult> results;
                                        		if (!YFCObject.isVoid(YFSSystem.getProperty("academy.ldap.activedirectory.enabled")) && 
                                        				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty("academy.ldap.activedirectory.enabled"))) {
                                        			//Active Directory User Search
                                        			logger.debug("inside if loop of Active Directory user-search if true");
                                        			results = ctx.search("dc=Academy,dc=com", "(sAMAccountName=" + sLoginID + ")", ctls);
                                        		} else {
                                        			//eDirectory User Search
                                        			logger.debug("inside else loop of eDirectory user-search if false");
                                                    results = ctx.search("", "cn=" + sLoginID,ctls);
                                        		}

                                                
                                                logger.debug("***********results"+results);

                                                if (!(results.hasMoreElements())) {
                                                            logger.debug("6 inside if line no 89");

                                                            logger.verbose("Invalid Credential");
                                                            yfe.setErrorCode("LDAP_002");
                                                            yfe.setErrorDescription("Unable to find user: " + sLoginID
                                                                                    + " in LDAP directory");
                                                            logger.debug("7");
                                                            throw yfe;

                                                } else {
                                                            logger.debug("inside else of line no 99");
                                                            // Get fully qualified "dn" for the logged in user
                                                            logger.verbose("UserID: " + sLoginID);
                                                            SearchResult sr = (SearchResult) results.next();
                                                            String userDN;
                                                            if (!YFCObject.isVoid(YFSSystem.getProperty("academy.ldap.activedirectory.enabled")) && 
                                                    				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty("academy.ldap.activedirectory.enabled"))) {
                                                    			//Active Directory UserDN
                                                    			logger.debug("inside if loop of Active Directory userDN if true");
                                                    			Attributes attrs = sr.getAttributes();
                                                                Attribute dnAttr = attrs.get("DistinguishedName");
                                                                userDN = (String) dnAttr.get();
                                                    		} else {
                                                    			//eDirectory UserDN
                                                    			logger.debug("inside else loop of eDirectory userDN if false");
                                                    			userDN = sr.getName();
                                                    		}
                                                            
                                                            
                                                            logger.verbose("User DN is:" + userDN);
                                                            logger.debug("9");
                                                            // Do User Bind using users "dn"
                                                            Hashtable userBindEnv = new Hashtable();
                                                            userBindEnv.put(Context.INITIAL_CONTEXT_FACTORY,
                                                                                    ldapFactory);
                                                            // adminUserDN = sAdminDN;
                                                            // String adminUserCredentials = sDNPassword;
                                                            // userBindEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
                                                            //userBindEnv.put(Context.PROVIDER_URL, YFSSystem
                                                            //                        .getProperty("yfs.security.ldap.url"));
                                                            userBindEnv.put(Context.PROVIDER_URL, ldapURL);
                                                            userBindEnv.put(Context.SECURITY_PRINCIPAL, userDN);
                                                            userBindEnv.put(Context.SECURITY_CREDENTIALS, sPassword);
                                                            /* Madhu commented ---userBindEnv.put(Context.SECURITY_PROTOCOL, "ssl");*/
                                                            logger.debug("10");
                                                            // Create the initial context for User bind.
                                                            userCtx = new InitialDirContext(userBindEnv);
                                                            logger.debug("11");
                                                            logger.verbose("User Bind Successful");
                                                }
                                    } catch (Exception e) {
                                                logger.verbose("Ldap error: " + e.toString());
                                                e.printStackTrace();
                                                throw new YFSException("LDAP Authentication Failed "
                                                                        + e.getMessage());
                                    } finally {
                                                if(ctx!= null) {
                                                            ctx.close();
                                                }
                                                if(userCtx != null) {
                                                            userCtx.close();
                                                }                                              
                                    }

                        }

                        logger.verbose("Authenticated");

                        return null;
            }

            public void formatAttributes(Attributes attrs) throws Exception {
                        if (attrs == null) {
                                    logger.verbose("This result has no attributes");
                        } else {
                                    logger.debug("inside else of line no 149");

                                    try {
                                                for (NamingEnumeration attrList = attrs.getAll(); attrList
                                                                        .hasMore();) {
                                                            Attribute attrib = (Attribute) attrList.next();
                                                            logger.verbose("ATTRIBUTE :" + attrib.getID());
                                                            for (NamingEnumeration e = attrib.getAll(); e.hasMore();)
                                                                        logger.verbose("\t\t        = " + e.next());
                                                }

                                    } catch (Exception e) {
                                                e.printStackTrace();
                                    }

                        }

            }
}

