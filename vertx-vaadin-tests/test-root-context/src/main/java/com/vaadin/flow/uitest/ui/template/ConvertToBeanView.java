/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.vaadin.flow.uitest.ui.template;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.ClientUpdateMode;
import com.vaadin.flow.templatemodel.Encode;
import com.vaadin.flow.templatemodel.ModelEncoder;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.ConvertToBeanView", layout = ViewTestLayout.class)
@Tag("convert-to-bean")
@JsModule("ConvertToBean.js")
public class ConvertToBeanView extends PolymerTemplate<ConvertToBeanView.ConvertToBeanModel> {

    public interface ConvertToBeanModel extends TemplateModel {
        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        Date getDate();

        @Encode(DateToBeanConverter.class)
        void setDate(Date date);

        void setMessage(String message);
    }

    public static class DateBean implements Serializable {
        private String day;
        private String month;
        private String year;

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }

    public static class DateToBeanConverter implements ModelEncoder<Date, DateBean> {

        @Override
        public DateBean encode(Date modelValue) {
            if (modelValue == null) {
                return null;
            }
            DateBean bean = new DateBean();
            bean.setDay(Integer.toString(modelValue.getDate()));
            bean.setMonth(Integer.toString(modelValue.getMonth() + 1));
            bean.setYear(Integer.toString(modelValue.getYear() + 1900));
            return bean;
        }

        @Override
        public Date decode(DateBean presentationValue) {
            if (presentationValue == null) {
                return null;
            }
            if (presentationValue.getYear() == null
                    || presentationValue.getDay() == null
                    || presentationValue.getMonth() == null) {
                return null;
            }

            return new Date(
                    Integer.valueOf(presentationValue.getYear()) - 1900,
                    Integer.valueOf(presentationValue.getMonth()) - 1,
                    Integer.valueOf(presentationValue.getDay()));
        }
    }

    public ConvertToBeanView() {
        setId("template");
    }

    @EventHandler
    private void submit() {
        Date date = getModel().getDate();
        getModel().setMessage(new SimpleDateFormat("dd.MM.yyyy").format(date));
    }
}
