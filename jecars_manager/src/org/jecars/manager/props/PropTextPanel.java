/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PropTextPanel.java
 *
 * Created on 31-okt-2009, 22:16:24
 */

package org.jecars.manager.props;

import org.jecars.manager.jcrProperty;
import org.jecars.manager.jcrTreeNode;

/**
 *
 * @author weert
 */
public class PropTextPanel extends PropPanel {

    /** Creates new form PropTextPanel */
    public PropTextPanel( jcrProperty pProp ) throws Exception {
        super(pProp);
        initComponents();
        refresh();
        return;
    }

    public void refresh() throws Exception {
      mPropName.setText( getProp().getName() );
      mPropValue.setText( getProp().getValueAsString() );
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

        jButton2 = new javax.swing.JButton();
        mPropName = new javax.swing.JLabel();
        mPropValue = new javax.swing.JTextField();
        mWriteProp = new javax.swing.JButton();
        mRemoveProp = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.jecars.manager.guiapp.class).getContext().getResourceMap(PropTextPanel.class);
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        setName("Form"); // NOI18N

        mPropName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        mPropName.setText(resourceMap.getString("mPropName.text")); // NOI18N
        mPropName.setName("mPropName"); // NOI18N

        mPropValue.setText(resourceMap.getString("mPropValue.text")); // NOI18N
        mPropValue.setName("mPropValue"); // NOI18N

        mWriteProp.setText(resourceMap.getString("mWriteProp.text")); // NOI18N
        mWriteProp.setName("mWriteProp"); // NOI18N
        mWriteProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mWritePropActionPerformed(evt);
            }
        });

        mRemoveProp.setText(resourceMap.getString("mRemoveProp.text")); // NOI18N
        mRemoveProp.setName("mRemoveProp"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mPropName, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mPropValue, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mWriteProp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mRemoveProp))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(mPropName)
                .addComponent(mPropValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(mRemoveProp)
                .addComponent(mWriteProp))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mWritePropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mWritePropActionPerformed

      try {
        writeProp( mPropValue.getText() );
      } catch( Exception e ) {
        
      }

      return;
    }//GEN-LAST:event_mWritePropActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel mPropName;
    private javax.swing.JTextField mPropValue;
    private javax.swing.JButton mRemoveProp;
    private javax.swing.JButton mWriteProp;
    // End of variables declaration//GEN-END:variables

}
