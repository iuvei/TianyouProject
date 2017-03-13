﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TianyouMultiChannel
{
    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            new MultiChannelForm().ShowDialog();
            
        }

        private void button2_Click(object sender, EventArgs e)
        {
            new PackingToolsForm().ShowDialog();
        }

        private void button3_Click(object sender, EventArgs e)
        {
            new ApkToolsForm().ShowDialog();
        }
    }
}
