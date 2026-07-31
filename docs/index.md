---
template: landing
title: Documentation
desc: Simple node-based NMR denoising 
items:
  - title: User guide
    desc: Install and run NodeNMR
    icon: school
    url: /usage/install
  - title: Developer docs
    desc: Implement new denoising methods
    icon: code
    url: /bindings/ffm-api
---

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="assets/banner-dark.svg"></source>
  <source media="(prefers-color-scheme: light)" srcset="assets/banner-light.svg"></source>
  <img src="assets/banner-light.svg"></img>
</picture>

NodeNMR is an open source tool for denoising NMR signals. It provides a simple node graph interface for building denoising pipelines without any code. It includes several denoising methods optimized to run on consumer hardware.
Currently, it supports only 1D datasets.