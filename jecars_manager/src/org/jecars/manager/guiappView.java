/*
 * guiappView.java
 */

package org.jecars.manager;

import java.awt.GridBagConstraints;
import javax.jcr.RepositoryException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jecars.CARS_Factory;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultClient;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Propertyable;
import org.jecars.client.local.JC_LocalClient;
import org.jecars.manager.props.PropTextPanel;

/**
 * The application's main frame.
 */
public class guiappView extends FrameView {

    static private String gLastPath = "";

    private jcrTreeModel mJCRTreeModel = null;
    private jcrTreeNode  mSelectedTreeNode = null;

    public guiappView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = guiapp.getApplication().getMainFrame();
            aboutBox = new guiappAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        guiapp.getApplication().show(aboutBox);
    }

    public void reinitTree() throws Exception {
      mJCRTree.setModel( new DefaultTreeModel( mJCRTreeModel.getRootTreeNode() ) );
      return;
    }

    public void refresh() {
      if (mJCRTreeModel instanceof jecarsTreeModel) {
        mJeCARSConnect.setText( "Disconnect" );
      } else {
        mJeCARSConnect.setText( "Connect" );
      }
      return;
    }

    /** objectInTreeSelected
     *
     * @param pObject
     * @throws Exception
     */
    public void objectInTreeSelected( Object pObject ) throws Exception {
      if (pObject instanceof jcrTreeNode) {
        mSelectedTreeNode = ((jcrTreeNode)pObject);
        final List<jcrProperty> props = mSelectedTreeNode.getProperties();
        mPropsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int ix = 5;
        for( jcrProperty prop : props ) {
          PropTextPanel ptp = new PropTextPanel( prop );
          gbc.gridy = ix++;
          mPropsPanel.add( ptp, gbc );
        }
      }
      mPropsPanel.revalidate();
      mPropsPanel.repaint();
      return;
    }

    public void setJcrTreeModel( final jcrTreeModel pJTM ) throws Exception {
      mJCRTreeModel = pJTM;
      reinitTree();
      return;
    }

    public void reportError( Throwable pE ) {
      pE.printStackTrace();
      return;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mPropsPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        mRefreshProps = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mJCRTree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        mJeCARSServer = new javax.swing.JComboBox();
        mJeCARSConnect = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        mJeCARSProperties = new javax.swing.JTextField();
        mSelectJecarsProps = new javax.swing.JButton();
        mJeCARSLocalConnect = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mJeCARSUsername = new javax.swing.JTextField();
        mJeCARSPassword = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        mJackrabbitConnect = new javax.swing.JButton();
        mJRPath = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        mSelectJRDirectory = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setDividerLocation(280);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jSplitPane2.setDividerLocation(200);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jTabbedPane2.setName("jTabbedPane2"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        mPropsPanel.setName("mPropsPanel"); // NOI18N
        mPropsPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane2.setViewportView(mPropsPanel);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.jecars.manager.guiapp.class).getContext().getResourceMap(guiappView.class);
        jPanel8.setBackground(resourceMap.getColor("jPanel8.background")); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        mRefreshProps.setText(resourceMap.getString("mRefreshProps.text")); // NOI18N
        mRefreshProps.setName("mRefreshProps"); // NOI18N
        mRefreshProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRefreshPropsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mRefreshProps)
                .addContainerGap(725, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mRefreshProps, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane2.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );

        jSplitPane2.setRightComponent(jPanel2);

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        mJCRTree.setName("mJCRTree"); // NOI18N
        mJCRTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                mJCRTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(mJCRTree);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );

        jSplitPane2.setLeftComponent(jPanel3);

        jSplitPane1.setTopComponent(jSplitPane2);

        jPanel1.setName("jPanel1"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jTabbedPane3.setName("jTabbedPane3"); // NOI18N

        jPanel9.setName("jPanel9"); // NOI18N

        jLabel1.setBackground(resourceMap.getColor("jLabel1.background")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.setOpaque(true);

        mJeCARSServer.setEditable(true);
        mJeCARSServer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "http://localhost:8080/cars" }));
        mJeCARSServer.setName("mJeCARSServer"); // NOI18N

        mJeCARSConnect.setIcon(resourceMap.getIcon("mJeCARSConnect.icon")); // NOI18N
        mJeCARSConnect.setText(resourceMap.getString("mJeCARSConnect.text")); // NOI18N
        mJeCARSConnect.setName("mJeCARSConnect"); // NOI18N
        mJeCARSConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mJeCARSConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mJeCARSServer, 0, 296, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1)
                        .addComponent(mJeCARSConnect, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(mJeCARSServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(mJeCARSConnect)
                .addContainerGap())
        );

        jTabbedPane3.addTab(resourceMap.getString("jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N

        jPanel10.setName("jPanel10"); // NOI18N

        jLabel4.setBackground(resourceMap.getColor("jLabel4.background")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setOpaque(true);

        mJeCARSProperties.setText(resourceMap.getString("mJeCARSProperties.text")); // NOI18N
        mJeCARSProperties.setName("mJeCARSProperties"); // NOI18N

        mSelectJecarsProps.setIcon(resourceMap.getIcon("mSelectJecarsProps.icon")); // NOI18N
        mSelectJecarsProps.setText(resourceMap.getString("mSelectJecarsProps.text")); // NOI18N
        mSelectJecarsProps.setName("mSelectJecarsProps"); // NOI18N
        mSelectJecarsProps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSelectJecarsPropsActionPerformed(evt);
            }
        });

        mJeCARSLocalConnect.setIcon(resourceMap.getIcon("mJeCARSLocalConnect.icon")); // NOI18N
        mJeCARSLocalConnect.setText(resourceMap.getString("mJeCARSLocalConnect.text")); // NOI18N
        mJeCARSLocalConnect.setName("mJeCARSLocalConnect"); // NOI18N
        mJeCARSLocalConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mJeCARSLocalConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(211, 211, 211))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(mJeCARSProperties, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mSelectJecarsProps)
                        .addGap(99, 99, 99))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(mJeCARSLocalConnect)
                        .addContainerGap(213, Short.MAX_VALUE))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mJeCARSProperties, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(mSelectJecarsProps))
                .addGap(33, 33, 33)
                .addComponent(mJeCARSLocalConnect)
                .addContainerGap())
        );

        jTabbedPane3.addTab(resourceMap.getString("jPanel10.TabConstraints.tabTitle"), jPanel10); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        mJeCARSUsername.setText(resourceMap.getString("mJeCARSUsername.text")); // NOI18N
        mJeCARSUsername.setName("mJeCARSUsername"); // NOI18N

        mJeCARSPassword.setText(resourceMap.getString("mJeCARSPassword.text")); // NOI18N
        mJeCARSPassword.setName("mJeCARSPassword"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mJeCARSPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .addComponent(mJeCARSUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                        .addGap(41, 41, 41))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(mJeCARSUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mJeCARSPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        mJackrabbitConnect.setIcon(resourceMap.getIcon("mJackrabbitConnect.icon")); // NOI18N
        mJackrabbitConnect.setText(resourceMap.getString("mJackrabbitConnect.text")); // NOI18N
        mJackrabbitConnect.setName("mJackrabbitConnect"); // NOI18N
        mJackrabbitConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mJackrabbitConnectActionPerformed(evt);
            }
        });

        mJRPath.setEditable(true);
        mJRPath.setName("mJRPath"); // NOI18N

        jLabel5.setBackground(resourceMap.getColor("jLabel5.background")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setOpaque(true);

        mSelectJRDirectory.setIcon(null);
        mSelectJRDirectory.setName("mSelectJRDirectory"); // NOI18N
        mSelectJRDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSelectJRDirectoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(mJRPath, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mSelectJRDirectory))
                    .addComponent(mJackrabbitConnect))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(0, 0, 0)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mSelectJRDirectory, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mJRPath, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addComponent(mJackrabbitConnect)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(354, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(117, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1017, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel1);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.jecars.manager.guiapp.class).getContext().getActionMap(guiappView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 849, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void mJeCARSConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mJeCARSConnectActionPerformed
      try {
        if (mJCRTreeModel==null) {
          jecarsTreeModel jtm = new jecarsTreeModel( (String)mJeCARSServer.getSelectedItem(), mJeCARSUsername.getText(), mJeCARSPassword.getPassword() );
          mJCRTreeModel = jtm;
        } else {
          mJCRTreeModel.disconnect();
        }
        reinitTree();
      } catch( Exception je ) {
        reportError( je );
      } finally {
      }
      return;
    }//GEN-LAST:event_mJeCARSConnectActionPerformed

    private void mJCRTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_mJCRTreeValueChanged
      TreePath tp = mJCRTree.getSelectionPath();
      try {
        objectInTreeSelected( tp.getLastPathComponent() );
      } catch( Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mJCRTreeValueChanged

    private void mJackrabbitConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mJackrabbitConnectActionPerformed
      try {
        if (mJCRTreeModel==null) {
          final jcrTreeModel jtm = new jcrTreeModel( (String)mJRPath.getSelectedItem() );
          mJCRTreeModel = jtm;
        } else {
          mJCRTreeModel.disconnect();
        }
        reinitTree();
      } catch( Exception je ) {
        reportError( je );
      } finally {
      }
      return;
    }//GEN-LAST:event_mJackrabbitConnectActionPerformed

    private void mRefreshPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRefreshPropsActionPerformed

      if (mSelectedTreeNode!=null) {
        try {
          mSelectedTreeNode.refresh();
          objectInTreeSelected( mSelectedTreeNode );
        } catch( Exception e ) {
          reportError( e );
        }
      }
      return;
    }//GEN-LAST:event_mRefreshPropsActionPerformed

    private void mJeCARSLocalConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mJeCARSLocalConnectActionPerformed
      try {
        if (mJCRTreeModel==null) {
          CARS_Factory.setJecarsPropertiesPath( mJeCARSProperties.getText() );
          final jecarsTreeModel jtm = new jecarsTreeModel( JC_LocalClient.JECARSLOCAL, mJeCARSUsername.getText(), mJeCARSPassword.getPassword() );
          mJCRTreeModel = jtm;
        } else {
          mJCRTreeModel.disconnect();
        }
        reinitTree();
      } catch( Exception je ) {
        reportError( je );
      } finally {
      }
    }//GEN-LAST:event_mJeCARSLocalConnectActionPerformed

    private void mSelectJRDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSelectJRDirectoryActionPerformed
      final JFileChooser fc = new JFileChooser( gLastPath );
      fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
      fc.setMultiSelectionEnabled( false );
      final int retVal = fc.showOpenDialog( getComponent() );
      if (retVal==JFileChooser.APPROVE_OPTION) {
        gLastPath = fc.getCurrentDirectory().getAbsolutePath();
        mJRPath.addItem( fc.getSelectedFile().getAbsolutePath() );
      }
      return;
    }//GEN-LAST:event_mSelectJRDirectoryActionPerformed

    private void mSelectJecarsPropsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSelectJecarsPropsActionPerformed
      final JFileChooser fc = new JFileChooser( gLastPath );
//      fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
      fc.setMultiSelectionEnabled( false );
      final int retVal = fc.showOpenDialog( getComponent() );
      if (retVal==JFileChooser.APPROVE_OPTION) {
        gLastPath = fc.getCurrentDirectory().getAbsolutePath();
        mJeCARSProperties.setText( fc.getSelectedFile().getAbsolutePath() );
      }
      return;
    }//GEN-LAST:event_mSelectJecarsPropsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTree mJCRTree;
    private javax.swing.JComboBox mJRPath;
    private javax.swing.JButton mJackrabbitConnect;
    private javax.swing.JButton mJeCARSConnect;
    private javax.swing.JButton mJeCARSLocalConnect;
    private javax.swing.JPasswordField mJeCARSPassword;
    private javax.swing.JTextField mJeCARSProperties;
    private javax.swing.JComboBox mJeCARSServer;
    private javax.swing.JTextField mJeCARSUsername;
    private javax.swing.JPanel mPropsPanel;
    private javax.swing.JButton mRefreshProps;
    private javax.swing.JButton mSelectJRDirectory;
    private javax.swing.JButton mSelectJecarsProps;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
