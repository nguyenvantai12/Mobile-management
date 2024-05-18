package doan.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import doan.models.Category;
import doan.models.Category_Product;
import doan.models.Message;
import doan.models.Product;
import doan.models.Vendor;
import doan.tcp.Client;
import doan.view.ManagerProductView;

public class ManagerProductController {
	public ManagerProductView product;
	public JTable table;
	public List<Product> listItem;
	public List<Category> listCategory;
	public List<Vendor> listVendor;
	public Product p;
	public Vendor vendor;
	public Category c;

	public ManagerProductController(ManagerProductView product) {
		super();
		this.product = product;
		this.product.addRemove(new addRemove());
		this.product.addSave(new addSave());
		this.product.addCreate(new addCreate());
		this.table = this.product.getTable();
		listCategory = GetCategory("GETALL", null);
		listVendor = GetVendor("GETALL", null);
		for (Vendor vd : listVendor) {
			product.getCombo().addItem(vd.getName());
		}
		table = updateTable();

		this.table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int selectedRowIndex = table.getSelectedRow();
				selectedRowIndex = table.convertRowIndexToModel(selectedRowIndex);
				p = listItem.get(selectedRowIndex);
				product.getTxtName().setText(p.getName());
				product.getDescription().setText(p.getDescription());
				product.getPrice().setText(String.valueOf(p.getPrice()));
				for (Product pro : listItem) {
					if (p.getVendorName().equals(pro.getVendorName())) {
						System.out.println(p.getVendorName());

						product.getCombo().setSelectedItem(p.getVendorName());
					}
				}

			}
		});

	}

	class addCreate implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String name = product.getTxtName().getText();
			String price = product.getPrice().getText();
			String vender = product.getCombo().getSelectedItem().toString();
			String des = product.getDescription().getText();

			for (Vendor vd : listVendor) {
				if (vd.getName().equals(vender)) {
					vendor =  vd;
				}
			}

			Product pr = new Product(0, name, des, Double.parseDouble(price), vendor, null);
			ObjectMapper mapper = new ObjectMapper();

			String orderJson = null;
			try {
				orderJson = mapper.writeValueAsString(pr);
			} catch (JsonProcessingException ex) {
				ex.printStackTrace();
			}
			Message<Product> msg = new Message<>("CREATE", Product.class, orderJson);
			try {
				System.out.println(msg.getObject());
				Message<?> res = Client.getInstance().Send(msg);
				System.out.println(res.getObject());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			updateTable();
		}

	}

	class addSave implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String name = product.getTxtName().getText();
			String price = product.getPrice().getText();
			String des = product.getDescription().getText();
			String vender = product.getCombo().getSelectedItem().toString();
			for (Vendor vd : listVendor) {
				if (vd.getName().equals(vender)) {
					vendor =  vd;
				}
			}
			p.setDescription(des);
			p.setName(name);
			p.setPrice(Double.parseDouble(price));
			p.setVendor(vendor);
			

			ObjectMapper mapper = new ObjectMapper();
			String orderJson = null;
			try {
				orderJson = mapper.writeValueAsString(p);
			} catch (JsonProcessingException ex) {
				ex.printStackTrace();
			}
			System.out.println(p.toString());
			Message<Product> msg = new Message<>("UPDATE", Product.class, orderJson);
			try {
				System.out.println(msg.getObject());
				Message<?> res = Client.getInstance().Send(msg);
				System.out.println(res.getObject());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			updateTable();

		}

	}

	class addRemove implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (p.getDescription().isEmpty()) {
				JOptionPane.showMessageDialog(product, "Vui lòng chọn 1 product");
			}
			ObjectMapper mapper = new ObjectMapper();
			String orderJson = null;
			try {
				orderJson = mapper.writeValueAsString(p);
			} catch (JsonProcessingException ex) {
				ex.printStackTrace();
			}
			Message<Product> msg = new Message<>("DELETE", Product.class, orderJson);
			try {
				System.out.println(msg.getObject());
				Message<?> res = Client.getInstance().Send(msg);
				System.out.println(res.getObject());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			updateTable();
		}

	}

	private List<Category> GetCategory(String operation, Category category) {
		ObjectMapper mapper = new ObjectMapper();
		List<Category> list = null;
		try {
			String categoryJson = mapper.writeValueAsString(category);

			Message<?> msg = new Message<>(operation, Category.class, categoryJson);
			msg = Client.getInstance().Send(msg);
			System.out.println(msg);
			list = mapper.readValue(msg.getObject(), new TypeReference<List<Category>>() {
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	private List<Product> GetData(String operation, Product product) {
		ObjectMapper mapper = new ObjectMapper();
		String prod;
		List<Product> list = null;
		try {
			prod = mapper.writeValueAsString(product);
			Message<?> msg = new Message<>(operation, Product.class, prod);
			msg = Client.getInstance().Send(msg);
			list = mapper.readValue(msg.getObject(), new TypeReference<List<Product>>() {
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	 
		return list;
	}

	private List<Vendor> GetVendor(String operation, Vendor vendor) {
		ObjectMapper mapper = new ObjectMapper();
		String prod;
		List<Vendor> list = null;
		try {
			prod = mapper.writeValueAsString(vendor);
			Message<?> msg = new Message<>(operation, Vendor.class, prod);
			msg = Client.getInstance().Send(msg);
			list = mapper.readValue(msg.getObject(), new TypeReference<List<Vendor>>() {
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public JTable updateTable() {
		listItem = GetData("GETALL", null);
		String[] listColumn = new String[] { "ID", "Name", "Price", "Description", "Vendor" };
		int columns = listColumn.length;
		DefaultTableModel dtm = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int rowIndenx, int colIdenx) {
				return false;
			}
		};
		dtm.setColumnIdentifiers(listColumn);
		Object[] obj = null;
		int num = listItem.size();
		Product p = null;
		if (num > 0) {
			for (int i = 0; i < num; i++) {
				p = listItem.get(i);
				obj = new Object[columns];
				obj[0] = p.getId();
				obj[1] = p.getName();
				obj[2] = p.getPrice();
				obj[3] = p.getDescription();
				obj[4] = p.getVendorName();
				dtm.addRow(obj);
			}
		}

	
		table.setModel(dtm);
		return table;
	}

}
